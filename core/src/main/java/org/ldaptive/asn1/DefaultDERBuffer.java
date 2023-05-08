/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;

/**
 * {@link DERBuffer} that uses a {@link ByteBuffer}.
 *
 * @author  Middleware Services
 */
public class DefaultDERBuffer implements DERBuffer
{

  /** Underlying byte buffer. */
  private final ByteBuffer buffer;


  /**
   * Creates a new default DER buffer. See {@link ByteBuffer#allocate(int)}.
   *
   * @param  capacity  of this buffer
   */
  public DefaultDERBuffer(final int capacity)
  {
    buffer = ByteBuffer.allocate(capacity);
  }


  /**
   * Creates a new default DER buffer. See {@link ByteBuffer#wrap(byte[])}.
   *
   * @param  array  contents of the buffer
   */
  public DefaultDERBuffer(final byte[] array)
  {
    buffer = ByteBuffer.wrap(array);
  }


  /**
   * Creates a new default DER buffer.
   *
   * @param  buf  existing byte buffer
   */
  public DefaultDERBuffer(final ByteBuffer buf)
  {
    buffer = buf;
  }


  /**
   * Creates a new default DER buffer and sets the initial position and limit.
   *
   * @param  buf  existing byte buffer
   * @param  pos  initial buffer position
   * @param  lim  initial buffer limit
   */
  public DefaultDERBuffer(final ByteBuffer buf, final int pos, final int lim)
  {
    buffer = buf;
    buffer.position(pos);
    buffer.limit(lim);
  }


  @Override
  public int position()
  {
    return buffer.position();
  }


  @Override
  public DERBuffer position(final int newPosition)
  {
    buffer.position(newPosition);
    return this;
  }


  @Override
  public int limit()
  {
    return buffer.limit();
  }


  @Override
  public int capacity()
  {
    return buffer.capacity();
  }


  @Override
  public DERBuffer limit(final int newLimit)
  {
    buffer.limit(newLimit);
    return this;
  }


  @Override
  public DERBuffer clear()
  {
    buffer.clear();
    return this;
  }


  @Override
  public byte get()
  {
    return buffer.get();
  }


  @Override
  public DERBuffer get(final byte[] dst)
  {
    buffer.get(dst);
    return this;
  }


  @Override
  public DERBuffer slice()
  {
    return new DefaultDERBuffer(buffer.slice());
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
