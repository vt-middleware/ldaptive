/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface for objects that can be made immutable after creation by invoking the {@link #freeze()} method.
 *
 * @author  Middleware Services
 */
public interface Freezable
{


  /** Freezes this object, making it immutable. */
  void freeze();


  /**
   * Determines whether this object is frozen, i.e. immutable.
   *
   * @return  True if {@link #freeze()} has been invoked, false otherwise.
   */
  boolean isFrozen();


  /**
   * Asserts that this object is in a state to permit mutations.
   * Classes that implement this interface should invoke this method prior to performing any mutation of internal state
   * as a means of implementing the "frozen" usage contract.
   *
   * @throws  IllegalStateException  if this object is frozen (i.e. immutable).
   */
  void assertMutable();
}
