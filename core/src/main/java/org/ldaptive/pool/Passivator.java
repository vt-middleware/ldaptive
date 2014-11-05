/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Provides an interface for passivating objects when they are checked back into
 * the pool.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 */
public interface Passivator<T>
{


  /**
   * Passivate the supplied object.
   *
   * @param  t  object
   *
   * @return  whether passivation was successful
   */
  boolean passivate(T t);
}
