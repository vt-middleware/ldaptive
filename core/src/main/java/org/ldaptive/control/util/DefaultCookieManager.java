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
package org.ldaptive.control.util;

/**
 * Cookie manager that stores a cookie in memory.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DefaultCookieManager implements CookieManager
{

  /** Control cookie. */
  private byte[] cookie;


  /** Creates a new default cookie manager. */
  public DefaultCookieManager() {}


  /**
   * Creates a new default cookie manager.
   *
   * @param  b  control cookie
   */
  public DefaultCookieManager(final byte[] b)
  {
    cookie = b;
  }


  /** {@inheritDoc} */
  @Override
  public byte[] readCookie()
  {
    return cookie;
  }


  /** {@inheritDoc} */
  @Override
  public void writeCookie(final byte[] b)
  {
    cookie = b;
  }
}
