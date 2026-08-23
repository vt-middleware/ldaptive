/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Wraps an existing {@link ConnectionFactory} in order to provide access to the same connection for multiple
 * operations. Unlike standard connection factories this class is intended to be short-lived and closed immediately when
 * the operations have completed.
 *
 * @author  Middleware Services
 */
public final class SingleConnectionFactoryWrapper implements ConnectionFactory, AutoCloseable
{

  /** Underlying connection factory. */
  private final ConnectionFactory factoryProxy;

  /** Connection proxy. */
  private final InvocationHandler proxy;

  /** Underlying connection,. */
  private final Connection connection;


  /**
   * Creates a new proxy connection factory.
   *
   * @param  factory  to get a connection to proxy
   *
   * @throws  LdapException  if a connection cannot be retrieved and opened
   */
  public SingleConnectionFactoryWrapper(final ConnectionFactory factory)
    throws LdapException
  {
    Connection conn = null;
    try {
      factoryProxy = factory;
      conn = factory.getConnection();
      conn.open();
      connection = conn;
      proxy = (object, method, args) -> {
        Object retValue = null;
        if (!"open".equals(method.getName()) && !"close".equals(method.getName())) {
          try {
            retValue = method.invoke(connection, args);
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          }
        }
        return retValue;
      };
    } catch (Exception e) {
      if (conn != null) {
        conn.close();
      }
      throw e;
    }
  }


  @Override
  public Connection getConnection() throws LdapException
  {
    return (Connection) Proxy.newProxyInstance(
      proxy.getClass().getClassLoader(),
      new Class[] {Connection.class},
      proxy);
  }


  @Override
  public ConnectionConfig getConnectionConfig()
  {
    return factoryProxy.getConnectionConfig();
  }


  @Override
  public void close()
  {
    connection.close();
  }
}
