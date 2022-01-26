/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.util.List;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;
import org.ldaptive.asn1.UniversalDERTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the input byte buffer until an entire message is available.
 *
 * @author  Middleware Services
 */
public class MessageFrameDecoder extends ByteToMessageDecoder
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
  {
    logger.trace("decoding {} bytes from {} on {}", in.readableBytes(), in, ctx);
    if (in.readableBytes() <= 2) {
      return;
    }

    final int readerIdx = in.readerIndex();
    final int writerIdx = in.writerIndex();
    int len = 0;
    try {
      final DERBuffer buffer = new NettyDERBuffer(in.readSlice(in.readableBytes()));
      len = readMessageLength(buffer);
    } finally {
      logger.trace("decoded message length of {} for {} on {}", len, in, ctx);
      // return the reader and writer indexes back to their initial position
      in.setIndex(readerIdx, writerIdx);
    }
    if (len > 0) {
      final ByteBuf retained = in.readRetainedSlice(len);
      logger.trace("read enough bytes from {} to decode message {} on {}", in, retained, ctx);
      out.add(retained);
      if (ctx != null) {
        ctx.fireUserEventTriggered(NettyConnection.MessageStatus.READ);
      }
    } else {
      logger.trace("could not read enough bytes from {} to decode message on {}", in, ctx);
    }
  }


  /**
   * Inspects the supplied buffer for a {@link UniversalDERTag#SEQ} tag and confirms the buffer contains enough bytes
   * for the length specified for the tag.
   *
   * @param  buffer  to read
   *
   * @return  DER message length
   *
   * @throws  IllegalArgumentException  if the buffer doesn't contain a SEQ tag
   */
  private int readMessageLength(final DERBuffer buffer)
  {
    final DERParser messageParser = new DERParser();
    final int tag = messageParser.readTag(buffer).getTagNo();
    if (UniversalDERTag.SEQ.getTagNo() != tag) {
      throw new IllegalArgumentException("Invalid message tag: " + tag);
    }
    try {
      final int len = messageParser.readLength(buffer);
      if (buffer.position() + len <= buffer.capacity()) {
        logger.trace("read entire message of length {} with buffer {}", len, buffer);
        return buffer.position() + len;
      }
      logger.trace("could not read entire message of length {} with buffer {}", len, buffer);
    } catch (IndexOutOfBoundsException e) {
      // it's possible to receive a multi-byte length without all the bytes
      // don't log that outcome as a warning
      logger.trace("error reading message length with buffer {}", buffer, e);
    } catch (Exception e) {
      logger.warn("Error reading message length with buffer {}", buffer, e);
    }
    return -1;
  }
}
