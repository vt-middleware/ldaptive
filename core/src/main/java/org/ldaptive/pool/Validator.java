/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Provides an interface for validating objects when they are in the pool.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 */
public interface Validator<T>
{


  /**
   * Validate the supplied object.
   *
   * @param  t  object
   *
   * @return  whether validation was successful
   */
  boolean validate(T t);
}
