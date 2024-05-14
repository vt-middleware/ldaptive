/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface for objects that can be made immutable when initialized.
 *
 * @author  Middleware Services
 */
public interface Immutable
{


  /** Make this object immutable. */
  void makeImmutable();


  /**
   * Returns whether this object is immutable.
   *
   * @return  whether this object is immutable
   */
  boolean isImmutable();


  /**
   * Verifies if this object is immutable.
   *
   * @throws  IllegalStateException  if this object is immutable
   */
  void checkImmutable();
}
