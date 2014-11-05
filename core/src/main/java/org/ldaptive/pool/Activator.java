/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Provides an interface for activating objects when they enter the pool.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
