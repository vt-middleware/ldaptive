/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Provides an interface for initializing connections after they are opened.
 *
 * @author  Middleware Services
 */
public interface ConnectionInitializer
{


  /**
   * Initialize the supplied connection.
   *
   * @param  conn  connection to initialize
   *
   * @return  result associated with the initialization or an empty result
   *
   * @throws  LdapException  if initialization fails
   */
  Result initialize(Connection conn)
    throws LdapException;
}
