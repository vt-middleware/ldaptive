/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.buffer.ByteBuf;
import org.ldaptive.asn1.DERBuffer;

/**
 * {@link DERBuffer} that uses a {@link ByteBuf}. Since {@link ByteBuf} does not have the concept of limit the writer
 * index is used to track the limit.
 *
 * @author  Middleware Services
 */
final class NettyDERBuffer implements DERBuffer
{

  /** Underlying byte buffer. */
  private final ByteBuf buffer;


  /**
   * Creates a new netty DER buffer.
   *
   * @param  buf  existing byte buf
   */
  NettyDERBuffer(final ByteBuf buf)
  {
    this(buf, 0, buf.capacity());
  }


  /**
   * Creates a new netty DER buffer and sets the initial position and limit.
   *
   * @param  buf  existing byte buf
   * @param  pos  initial buffer position
   * @param  lim  initial buffer limit
   */
  NettyDERBuffer(final ByteBuf buf, final int pos, final int lim)
  {
    buffer = buf;
    buffer.setIndex(pos, lim);
  }


  @Override
  public int position()
  {
    return buffer.readerIndex();
  }


  @Override
  public DERBuffer position(final int newPosition)
  {
    // will throw if you attempt to set the reader index greater than the writer index
    // prefer #positionAndLimit if chaining #position and #limit
    try {
      buffer.readerIndex(newPosition);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(e);
    }
    return this;
  }


  @Override
  public int limit()
  {
    return buffer.writerIndex();
  }


  @Override
  public int capacity()
  {
    return buffer.capacity();
  }


  @Override
  public DERBuffer limit(final int newLimit)
  {
    // will throw if you attempt to set the writer index less than the reader index
    // prefer #positionAndLimit if chaining #position and #limit
    try {
      buffer.writerIndex(newLimit);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(e);
    }
    return this;
  }


  @Override
  public DERBuffer positionAndLimit(final int newPosition, final int newLimit)
  {
    if (newPosition > newLimit) {
      throw new IllegalArgumentException("newPosition must be less than or equal to newLimit");
    }
    try {
      // avoid reader > writer and writer < reader exceptions
      if (newPosition > buffer.writerIndex()) {
        buffer.writerIndex(newLimit).readerIndex(newPosition);
      } else {
        buffer.readerIndex(newPosition).writerIndex(newLimit);
      }
      return this;
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException(e);
    }
  }


  @Override
  public DERBuffer clear()
  {
    buffer.setIndex(0, buffer.capacity());
    return this;
  }


  @Override
  public byte get()
  {
    return buffer.readByte();
  }


  @Override
  public DERBuffer get(final byte[] dst)
  {
    buffer.readBytes(dst);
    return this;
  }


  @Override
  public DERBuffer slice()
  {
    return new NettyDERBuffer(buffer.slice(position(), remaining()));
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "pos=" + position() + ", " +
      "lim=" + limit() + ", " +
      "cap=" + capacity();
  }
}
