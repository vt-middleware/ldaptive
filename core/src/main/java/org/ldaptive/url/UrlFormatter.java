/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

/**
 * Interface for formatting LDAP URLS.
 *
 * @author  Middleware Services
 */
public interface UrlFormatter
{


  /**
   * Returns a string representation of the supplied LDAP URL.
   *
   * @param  url  to format
   *
   * @return  formatted LDAP URL
   */
  String format(Url url);
}
