/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;

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
    buffer = LdapUtils.assertNotNullArg(buf, "Buffer cannot be null");
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
    buffer = LdapUtils.assertNotNullArg(buf, "Buffer cannot be null");
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
    // will throw if you attempt to set the position index greater than the limit
    // prefer #positionAndLimit if chaining #position and #limit
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
    // prefer #positionAndLimit if chaining #position and #limit
    buffer.limit(newLimit);
    return this;
  }


  @Override
  public DERBuffer positionAndLimit(final int newPosition, final int newLimit)
  {
    if (newPosition > newLimit) {
      throw new IllegalArgumentException("newPosition must be less than or equal to newLimit");
    }
    // set the limit first to avoid position > limit exception
    buffer.limit(newLimit).position(newPosition);
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
