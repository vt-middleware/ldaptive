/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.net.SocketAddress;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CoalescingBufferQueue;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty handler that uses a {@link SaslClient} to wrap and unwrap requests and responses.
 *
 * @author  Middleware Services
 */
public class SaslHandler extends SimpleChannelInboundHandler<ByteBuf> implements ChannelOutboundHandler
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(SaslHandler.class);

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
  public void handlerRemoved(final ChannelHandlerContext ctx)
    throws Exception
  {
    dispose();
  }


  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf msg)
    throws Exception
  {
    final byte[] bytes = new byte[msg.readableBytes()];
    msg.readBytes(bytes);
    ctx.fireChannelRead(Unpooled.wrappedBuffer(saslClient.unwrap(bytes, 0, bytes.length)));
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
    if (!(msg instanceof ByteBuf)) {
      final UnsupportedMessageTypeException exception = new UnsupportedMessageTypeException(msg, ByteBuf.class);
      ReferenceCountUtil.safeRelease(msg);
      promise.setFailure(exception);
    }
    queue.add((ByteBuf) msg, promise);
  }


  @Override
  public void flush(final ChannelHandlerContext ctx)
    throws Exception
  {
    if (queue.isEmpty()) {
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
      ctx.writeAndFlush(Unpooled.wrappedBuffer(wrappedBytes), promise);
    } finally {
      if (buf != null) {
        ReferenceCountUtil.safeRelease(buf);
      }
    }
  }
}
