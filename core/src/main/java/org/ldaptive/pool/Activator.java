/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Provides an interface for activating objects when they are checked out from the pool.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 */
public interface Activator<T>
{


  /**
   * Activate the supplied object.
   *
   * @param  t  object
   *
   * @return  whether activation was successful
   */
  boolean activate(T t);
}
