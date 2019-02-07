/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderFactory;

/**
 * Creates a single connection which is proxied for LDAP operations.
 *
 * @author  Middleware Services
 */
public class SingleConnectionFactory extends DefaultConnectionFactory
{


  /** The connection used by this factory. */
  private Connection connection;

  /** The proxy used by this factory. */
  private Connection proxy;


  /** Default constructor. */
  public SingleConnectionFactory() {}


  /**
   * Creates a new single connection factory.
   *
   * @param  ldapUrl  to connect to
   */
  public SingleConnectionFactory(final String ldapUrl)
  {
    this(new ConnectionConfig(ldapUrl));
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  cc  connection configuration
   */
  public SingleConnectionFactory(final ConnectionConfig cc)
  {
    this(cc, ProviderFactory.getProvider());
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  cc  connection configuration
   * @param  p  provider
   */
  public SingleConnectionFactory(final ConnectionConfig cc, final Provider p)
  {
    super(cc, p);
  }


  /**
   * Prepares this factory for use.
   *
   * @throws   LdapException  if the connection cannot be opened
   */
  public void initialize()
    throws LdapException
  {
    connection = super.getConnection();
    connection.open();
    proxy = (Connection) Proxy.newProxyInstance(
      Connection.class.getClassLoader(),
      new Class[] {Connection.class},
      new ConnectionProxy(connection));
  }


  @Override
  public Connection getConnection()
  {
    return proxy;
  }


  @Override
  public void close()
  {
    if (connection != null) {
      connection.close();
    }
    super.close();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("provider=").append(getProvider()).append(", ")
      .append("config=").append(getConnectionConfig()).append("]").toString();
  }


  /**
   * Contains the connection used by this factory.
   */
  protected static class ConnectionProxy implements InvocationHandler
  {

    /** hash code seed. */
    private static final int HASH_CODE_SEED = 509;

    /** Underlying connection. */
    private final Connection conn;


    /**
     * Creates a new connection proxy.
     *
     * @param  c  connection to proxy
     */
    public ConnectionProxy(final Connection c)
    {
      conn = c;
    }


    @Override
    public boolean equals(final Object o)
    {
      if (o == this) {
        return true;
      }
      if (o instanceof ConnectionProxy) {
        final ConnectionProxy v = (ConnectionProxy) o;
        return LdapUtils.areEqual(conn, v.conn);
      }
      return false;
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, conn);
    }


    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable
    {
      Object retValue = null;
      if (!"open".equals(method.getName()) && !"close".equals(method.getName())) {
        try {
          retValue = method.invoke(conn, args);
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        }
      }
      return retValue;
    }
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder extends DefaultConnectionFactory.Builder
  {

    private final SingleConnectionFactory object = new SingleConnectionFactory();


    protected Builder() {}


    public Builder config(final ConnectionConfig cc)
    {
      object.setConnectionConfig(cc);
      return this;
    }


    public SingleConnectionFactory build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
