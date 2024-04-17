/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

/**
 * Interface for parsing LDAP URLs.
 *
 * @author  Middleware Services
 */
public interface UrlParser
{

  /**
   * Parses the supplied URL into a {@link Url}.
   *
   * @param  url  to parse
   *
   * @return  LdapURL object
   */
  Url parse(String url);
}
