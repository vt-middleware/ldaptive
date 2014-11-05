/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

/**
 * Cookie manager that stores a cookie in memory.
 *
 * @author  Middleware Services
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
