/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Byte buffer used for DER parsing.
 *
 * @author  Middleware Services
 */
public interface DERBuffer
{


  /**
   * Returns this buffer's position.
   *
   * @return  position of this buffer
   */
  int position();


  /**
   * Sets this buffer's position.
   *
   * @param  newPosition
   *         The new position value; must be non-negative
   *         and no larger than the current limit
   *
   * @return  This buffer
   *
   * @throws  IllegalArgumentException  if the preconditions on newPosition do not hold
   */
  DERBuffer position(int newPosition);


  /**
   * Returns this buffer's limit.
   *
   * @return  limit of this buffer
   */
  int limit();


  /**
   * Sets this buffer's limit.
   *
   * @param  newLimit
   *         The new limit value; must be non-negative
   *         and no larger than this buffer's capacity
   *
   * @return  This buffer
   *
   * @throws  IllegalArgumentException  if the preconditions on newLimit do not hold
   */
  DERBuffer limit(int newLimit);


  /**
   * Sets this buffer's position and limit at the same time. Different buffer implementations may impose different
   * constraints when moving both the position and limit indexes at the same time. Consequently, you may not be able to
   * chain the {@link #position(int)} and {@link #limit(int)} methods correctly for all implementations.
   *
   * @param  newPosition
   *         The new position value; must be non-negative
   *         and no larger than the current limit
   * @param  newLimit
   *         The new limit value; must be non-negative
   *         and no larger than this buffer's capacity
   *
   * @return  This buffer
   *
   * @throws  IllegalArgumentException  if the preconditions on newPosition and newLimit do not hold
   */
  DERBuffer positionAndLimit(int newPosition, int newLimit);


  /**
   * Sets the position to zero and the limit to the capacity.
   *
   * <p>This method does not actually erase the data in the buffer.</p>
   *
   * @return  This buffer
   */
  DERBuffer clear();


  /**
   * Returns the number of elements between the current position and the limit.
   *
   * @return  number of elements remaining in this buffer
   */
  default int remaining()
  {
    return limit() - position();
  }


  /**
   * Returns whether there are any elements between the current position and the limit.
   *
   * @return  true iff there is at least one element remaining in this buffer
   */
  default boolean hasRemaining()
  {
    return position() < limit();
  }


  /**
   * Returns this buffer's capacity.
   *
   * @return  capacity of this buffer
   */
  int capacity();


  /**
   * Relative <i>get</i> method. Reads the byte at this buffer's current position and then increments the position.
   *
   * @return  byte at the buffer's current position
   */
  byte get();


  /**
   * Relative bulk <i>get</i> method.
   *
   * @param  dst  destination array
   *
   * @return  This buffer
   */
  DERBuffer get(byte[] dst);


  /**
   * Returns the bytes remaining in the buffer. Those bytes between {@link #position()} and {@link #limit()}.
   *
   * @return  remaining bytes
   */
  default byte[] getRemainingBytes()
  {
    final byte[] b = new byte[remaining()];
    get(b);
    return b;
  }


  /**
   * Creates a new DER buffer whose content is a shared sub-sequence of this buffer's content.
   *
   * <p>The content of the new buffer will start at this buffer's current position. Changes to this buffer's content
   * will be visible in the new buffer, and vice versa; the two buffers' position and limit will be independent.</p>
   *
   * <p>The new buffer's position will be zero, its capacity and its limit will be the number of bytes remaining in this
   * buffer.</p>
   *
   * @return  The new byte buffer
   */
  DERBuffer slice();
}
