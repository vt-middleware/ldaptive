/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface for objects that manage an instance of connection factory.
 *
 * @author  Middleware Services
 */
public interface ConnectionFactoryManager
{


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  ConnectionFactory getConnectionFactory();


  /**
   * Sets the connection factory.
   *
   * @param  cf  connection factory
   */
  void setConnectionFactory(ConnectionFactory cf);
}
