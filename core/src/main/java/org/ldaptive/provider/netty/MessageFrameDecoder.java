/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

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
    logger.trace("decoding {} bytes", in.readableBytes());
    if (in.readableBytes() > 2) {
      final int readerIdx = in.readerIndex();
      final int writerIdx = in.writerIndex();
      final DERBuffer buffer = new NettyDERBuffer(in.readSlice(in.readableBytes()));
      final int len = readMessageLength(buffer);
      logger.trace("decoded length of {}", len);
      if (len > 0) {
        in.readerIndex(readerIdx);
        out.add(in.readRetainedSlice(len));
      } else {
        in.setIndex(readerIdx, writerIdx);
      }
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
        return buffer.position() + len;
      }
    } catch (Exception e) {
      logger.trace("Error reading message length", e);
    }
    return -1;
  }
}
