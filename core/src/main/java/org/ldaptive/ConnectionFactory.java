/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Factory for creating connections.
 *
 * @author  Middleware Services
 */
public interface ConnectionFactory extends AutoCloseable
{
  /**
   * @return Connection configuration for this factory.
   */
  ConnectionConfig getConnectionConfig();


  /**
   * @return Set of LDAP URLs that connections produced by this factory can connect to.
   */
  LdapURLSet getLdapURLSet();


  /**
   * Creates a new connection.
   *
   * @return  connection
   *
   * @throws  LdapException  if a connection cannot be returned
   */
  Connection getConnection() throws LdapException;


  /** Free any resources associated with this factory. */
  default void close() {}
}
