/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.transport.mock.MockConnection;

/**
 * Mock connection factory for testing.
 *
 * @author  Middleware Services
 */
public class MockConnectionFactory implements ConnectionFactory
{

  /** Connection config. */
  private ConnectionConfig connectionConfig;

  /** Mock connection. */
  private MockConnection connection;

  /** Whether this connection factory is open. */
  private boolean isOpen = true;


  /**
   * Creates a new mock connection factory.
   *
   * @param  cc  connection config
   */
  public MockConnectionFactory(final ConnectionConfig cc)
  {
    connectionConfig = cc;
  }


  /**
   * Creates a new mock connection factory.
   *
   * @param  conn  mock connection
   */
  public MockConnectionFactory(final MockConnection conn)
  {
    connection = conn;
  }


  @Override
  public Connection getConnection() throws LdapException
  {
    if (isOpen) {
      return connection != null ? connection : new MockConnection(connectionConfig);
    }
    throw new IllegalStateException("Connection factory is closed.");
  }


  @Override
  public ConnectionConfig getConnectionConfig()
  {
    return connectionConfig;
  }


  @Override
  public void close()
  {
    isOpen = false;
  }


  /**
   * Returns whether {@link #close()} has been invoked.
   *
   * @return  whether close has been invoked
   */
  public boolean isOpen()
  {
    return isOpen;
  }
}
