/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Provides an interface for validating objects when they are in the pool.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
