/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.net.SocketAddress;
import java.util.List;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CoalescingBufferQueue;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty handler that uses a {@link SaslClient} to wrap and unwrap requests and responses.
 *
 * @author  Middleware Services
 */
public class SaslHandler extends ByteToMessageDecoder implements ChannelOutboundHandler
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Underlying SASL client. */
  private final SaslClient saslClient;

  /** To manage requests. */
  private CoalescingBufferQueue queue;


  /**
   * Creates a new SASL handler.
   *
   * @param  sc  SASL client
   */
  public SaslHandler(final SaslClient sc)
  {
    saslClient = sc;
  }


  @Override
  public void handlerAdded(final ChannelHandlerContext ctx)
    throws Exception
  {
    queue = new CoalescingBufferQueue(ctx.channel());
  }


  @Override
  public void handlerRemoved0(final ChannelHandlerContext ctx)
    throws Exception
  {
    dispose();
  }

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
    throws Exception
  {
    logger.trace("decoding {} bytes from {} on {}", in.readableBytes(), in, ctx);
    // CheckStyle:MagicNumber OFF
    if (in.readableBytes() <= 4) {
      return;
    }
    // CheckStyle:MagicNumber ON

    final int readerIdx = in.readerIndex();
    final int writerIdx = in.writerIndex();
    final int len = in.readInt();
    if (in.readableBytes() < len) {
      in.setIndex(readerIdx, writerIdx);
      logger.trace("could not read enough bytes from {} to decode message on {}", in, ctx);
      return;
    }

    final byte[] wrappedBytes = new byte[len];
    in.readBytes(wrappedBytes);
    final byte[] unwrapped = saslClient.unwrap(wrappedBytes, 0, wrappedBytes.length);
    ctx.fireChannelRead(Unpooled.wrappedBuffer(unwrapped));
    logger.trace("fired channel read for unwrapped message of length {}", unwrapped.length);
  }


  @Override
  public void bind(final ChannelHandlerContext ctx, final SocketAddress localAddress, final ChannelPromise promise)
    throws Exception
  {
    ctx.bind(localAddress, promise);
  }


  @Override
  public void connect(
    final ChannelHandlerContext ctx,
    final SocketAddress remoteAddress,
    final SocketAddress localAddress,
    final ChannelPromise promise)
    throws Exception
  {
    ctx.connect(remoteAddress, localAddress, promise);
  }


  @Override
  public void disconnect(final ChannelHandlerContext ctx, final ChannelPromise promise)
    throws Exception
  {
    dispose();
    ctx.close(promise);
  }


  @Override
  public void close(final ChannelHandlerContext ctx, final ChannelPromise promise)
    throws Exception
  {
    dispose();
    ctx.close(promise);
  }


  /**
   * Disposes the SASL client and releases all buffers from the queue.
   */
  private void dispose()
  {
    logger.trace("disposing {} for client {} with queue {}", this, saslClient, queue);
    try {
      saslClient.dispose();
    } catch (SaslException e) {
      logger.warn("Error disposing of SASL client", e);
    }

    if (queue != null && !queue.isEmpty()) {
      queue.releaseAndFailAll(new ChannelException("SASL client closed"));
    }
    queue = null;
  }


  @Override
  public void deregister(final ChannelHandlerContext ctx, final ChannelPromise promise)
    throws Exception
  {
    ctx.deregister(promise);
  }


  @Override
  public void read(final ChannelHandlerContext ctx)
    throws Exception
  {
    ctx.read();
  }


  @Override
  public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise)
    throws Exception
  {
    logger.trace("write {} message of {} with queue {}", ctx, msg, queue);
    if (!(msg instanceof ByteBuf)) {
      final UnsupportedMessageTypeException exception = new UnsupportedMessageTypeException(msg, ByteBuf.class);
      ReferenceCountUtil.safeRelease(msg);
      promise.setFailure(exception);
    } else if (queue == null) {
      ReferenceCountUtil.safeRelease(msg);
      promise.setFailure(new IllegalStateException("Queue is null, handler has been removed"));
    } else {
      queue.add((ByteBuf) msg, promise);
      logger.trace("added message {} to queue {}", msg, queue);
    }
  }


  @Override
  public void flush(final ChannelHandlerContext ctx)
    throws Exception
  {
    logger.trace("flushing {} with queue {}", ctx, queue);
    if (ctx.isRemoved() || queue.isEmpty()) {
      return;
    }
    ByteBuf buf = null;
    try {
      final ChannelPromise promise = ctx.newPromise();
      final int readableBytes = queue.readableBytes();
      buf = queue.remove(readableBytes, promise);
      final byte[] bytes = new byte[readableBytes];
      buf.readBytes(bytes);
      final byte[] wrappedBytes = saslClient.wrap(bytes, 0, bytes.length);
      final ByteBuf wrappedBuf = Unpooled.buffer(wrappedBytes.length + 4);
      wrappedBuf.writeInt(wrappedBytes.length).writeBytes(wrappedBytes);
      ctx.writeAndFlush(wrappedBuf, promise);
      logger.trace("write and flush of {} for wrapped message of length {}", ctx, wrappedBytes.length);
    } finally {
      if (buf != null) {
        ReferenceCountUtil.safeRelease(buf);
      }
    }
  }
}
