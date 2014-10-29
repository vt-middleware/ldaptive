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
 * Thrown when the pool is empty and no new requests can be serviced.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PoolExhaustedException extends PoolException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -2092251274513447389L;


  /**
   * Creates a new pool exhausted exception.
   *
   * @param  msg  describing this exception
   */
  public PoolExhaustedException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new pool exhausted exception.
   *
   * @param  e  pooling specific exception
   */
  public PoolExhaustedException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new pool exhausted exception.
   *
   * @param  msg  describing this exception
   * @param  e  pooling specific exception
   */
  public PoolExhaustedException(final String msg, final Exception e)
  {
    super(msg, e);
  }
}
