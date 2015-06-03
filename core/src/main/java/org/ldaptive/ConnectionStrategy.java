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

  /** default strategy. */
  ConnectionStrategy DEFAULT = new DefaultConnectionStrategy();

  /** active-passive strategy. */
  ConnectionStrategy ACTIVE_PASSIVE = new ActivePassiveConnectionStrategy();

  /** round robin strategy. */
  ConnectionStrategy ROUND_ROBIN = new RoundRobinConnectionStrategy();

  /** random strategy. */
  ConnectionStrategy RANDOM = new RandomConnectionStrategy();


  /**
   * Returns an ordered list of URLs to attempt to open.
   *
   * @param  metadata  which can be used to produce the URL list
   *
   * @return  array of ldap URLs
   */
  String[] getLdapUrls(ConnectionFactoryMetadata metadata);
}
