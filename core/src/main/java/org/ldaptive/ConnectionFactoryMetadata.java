/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface to describe the state of the connection factory. Used by {@link ConnectionStrategy} to produce LDAP URLs.
 *
 * @author  Middleware Services
 */
public interface ConnectionFactoryMetadata
{


  /**
   * Returns the LDAP URL the provider connection factory is using. May be space delimited for multiple URLs.
   *
   * @return  ldap url
   */
  String getLdapUrl();


  /**
   * Returns the number of times the provider connection factory has created a connection.
   *
   * @return  connection count
   */
  int getConnectionCount();
}
