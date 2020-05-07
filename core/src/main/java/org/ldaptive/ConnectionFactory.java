/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface for connection factories.
 *
 * @author  Middleware Services
 */
public interface ConnectionFactory
{


  /**
   * Creates a new connection.
   *
   * @return  connection
   *
   * @throws  LdapException  if a connection cannot be returned
   */
  Connection getConnection()
    throws LdapException;


  /**
   * Returns the connection configuration used to create connections.
   *
   * @return  connection config
   */
  ConnectionConfig getConnectionConfig();


  /** Free any resources associated with this factory. */
  void close();
}
