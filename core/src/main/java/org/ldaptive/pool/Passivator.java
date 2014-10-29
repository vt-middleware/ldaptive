/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.pool;

/**
 * Provides an interface for passivating objects when they are checked back into
 * the pool.
 *
 * @param  <T>  type of object being pooled
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
