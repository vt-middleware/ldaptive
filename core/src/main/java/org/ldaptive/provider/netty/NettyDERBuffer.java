/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

import io.netty.buffer.ByteBuf;
import org.ldaptive.asn1.DERBuffer;

/**
 * {@link DERBuffer} that uses a {@link ByteBuf}. Since {@link ByteBuf} does not have the concept of limit the writer
 * index is used to track the limit.
 *
 * @author  Middleware Services
 */
public class NettyDERBuffer implements DERBuffer
{

  /** Underlying byte buffer. */
  private final ByteBuf buffer;


  /**
   * Creates a new netty DER buffer.
   *
   * @param  buf  existing byte buf
   */
  public NettyDERBuffer(final ByteBuf buf)
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
  public NettyDERBuffer(final ByteBuf buf, final int pos, final int lim)
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
    buffer.readerIndex(newPosition);
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
    buffer.writerIndex(newLimit);
    if (buffer.readerIndex() > newLimit) {
      buffer.readerIndex(newLimit);
    }
    return this;
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
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("pos=").append(position()).append(", ")
      .append("lim=").append(limit()).append(", ")
      .append("cap=").append(capacity()).toString();
  }
}
