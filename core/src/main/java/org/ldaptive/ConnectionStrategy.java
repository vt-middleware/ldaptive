/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface to describe various connection strategies. Each strategy returns an ordered list of URLs to attempt when
 * opening a connection.
 *
 * @author  Middleware Services
 */
public interface ConnectionStrategy
{


  /**
   * Returns an ordered list of URLs to attempt to open.
   *
   * @param  metadata  which can be used to produce the URL list
   *
   * @return  array of ldap URLs
   */
  String[] getLdapUrls(ConnectionFactoryMetadata metadata);
}
