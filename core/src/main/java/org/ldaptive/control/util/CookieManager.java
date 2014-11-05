/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

/**
 * Interface for the reading and writing of control related cookies.
 *
 * @author  Middleware Services
 */
public interface CookieManager
{


  /**
   * Read and return a cookie from storage.
   *
   * @return  cookie read from storage
   */
  byte[] readCookie();


  /**
   * Writes a cookie to storage.
   *
   * @param  cookie  to write
   */
  void writeCookie(byte[] cookie);
}
