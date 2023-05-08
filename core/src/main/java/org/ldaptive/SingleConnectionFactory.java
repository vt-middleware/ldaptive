/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ldaptive.transport.Transport;
import org.ldaptive.transport.TransportFactory;

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

  /** Whether {@link #initialize()} has been successfully invoked. */
  private boolean initialized;

  /** Whether {@link #initialize()} should throw if the connection cannot be opened. */
  private boolean failFastInitialize = true;

  /** Whether {@link #initialize()} should occur on a separate thread. */
  private boolean nonBlockingInitialize;

  /** Executor for scheduling {@link #initialize()}. */
  private ExecutorService initializeExecutor;


  /** Default constructor. */
  public SingleConnectionFactory()
  {
    super(TransportFactory.getTransport(SingleConnectionFactory.class));
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  t  transport
   */
  public SingleConnectionFactory(final Transport t)
  {
    super(t);
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  ldapUrl  to connect to
   */
  public SingleConnectionFactory(final String ldapUrl)
  {
    super(ldapUrl, TransportFactory.getTransport(SingleConnectionFactory.class));
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  ldapUrl  to connect to
   * @param  t  transport
   */
  public SingleConnectionFactory(final String ldapUrl, final Transport t)
  {
    super(ldapUrl, t);
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  cc  connection configuration
   */
  public SingleConnectionFactory(final ConnectionConfig cc)
  {
    super(cc, TransportFactory.getTransport(SingleConnectionFactory.class));
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  cc  connection configuration
   * @param  t  transport
   */
  public SingleConnectionFactory(final ConnectionConfig cc, final Transport t)
  {
    super(cc, t);
  }


  /**
   * Returns whether {@link #initialize()} should throw if the connection cannot be opened.
   *
   * @return  whether {@link #initialize()} should throw
   */
  public boolean getFailFastInitialize()
  {
    return failFastInitialize;
  }


  /**
   * Sets whether {@link #initialize()} should throw if the connection cannot be opened.
   *
   * @param  b  whether {@link #initialize()} should throw
   */
  public void setFailFastInitialize(final boolean b)
  {
    failFastInitialize = b;
  }


  /**
   * Returns whether {@link #initialize()} should execute on a separate thread.
   *
   * @return  whether {@link #initialize()} should block
   */
  public boolean getNonBlockingInitialize()
  {
    return nonBlockingInitialize;
  }


  /**
   * Sets whether {@link #initialize()} should execute on a separate thread.
   *
   * @param  b  whether {@link #initialize()} should block
   */
  public void setNonBlockingInitialize(final boolean b)
  {
    nonBlockingInitialize = b;
  }


  /**
   * Returns whether this factory has been initialized.
   *
   * @return  whether this factory has been initialized
   */
  public boolean isInitialized()
  {
    return initialized;
  }


  /**
   * Prepares this factory for use.
   *
   * @throws   LdapException  if the connection cannot be opened
   */
  public synchronized void initialize()
    throws LdapException
  {
    if (initialized) {
      throw new IllegalStateException("Connection factory is already initialized");
    }
    if (nonBlockingInitialize) {
      if (initializeExecutor == null) {
        initializeExecutor = Executors.newCachedThreadPool(
          r -> {
            final Thread t = new Thread(r, "ldaptive-" + getClass().getSimpleName() + "@" + hashCode());
            t.setDaemon(true);
            return t;
          });
      }
      initializeExecutor.execute(
        () -> {
          try {
            initializeInternal();
          } catch (LdapException e) {
            logger.debug("Execution of initialize failed", e);
          }
        });
    } else {
      initializeInternal();
    }
  }


  /**
   * Attempts to open the connection and establish the proxy.
   *
   * @throws  LdapException  if {@link Connection#open()} fails and {@link #failFastInitialize} is true
   */
  private synchronized void initializeInternal()
    throws LdapException
  {
    if (!initialized) {
      try {
        connection = super.getConnection();
        connection.open();
        proxy = (Connection) Proxy.newProxyInstance(
          Connection.class.getClassLoader(),
          new Class[] {Connection.class},
          new ConnectionProxy(connection));
        initialized = true;
        logger.info("Factory initialized {}", this);
      } catch (LdapException e) {
        if (failFastInitialize) {
          throw e;
        }
        logger.warn("Could not initialize connection factory", e);
      }
    } else {
      logger.debug("Factory already initialized");
    }
  }


  @Override
  public Connection getConnection()
  {
    if (!initialized) {
      throw new IllegalStateException("Connection factory is not initialized");
    }
    return proxy;
  }


  @Override
  public synchronized void close()
  {
    if (connection != null) {
      connection.close();
    }
    if (initializeExecutor != null) {
      try {
        initializeExecutor.shutdown();
      } finally {
        initializeExecutor = null;
      }
    }
    super.close();
    initialized = false;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "transport=" + getTransport() + ", " +
      "config=" + getConnectionConfig() + ", " +
      "failFastInitialize=" + failFastInitialize + ", " +
      "nonBlockingInitialize=" + nonBlockingInitialize + ", " +
      "initialized=" + initialized + "]";
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


  /**
   * Creates a builder for this class.
   *
   * @param  t  transport
   *
   * @return  new builder
   */
  public static Builder builder(final Transport t)
  {
    return new Builder(t);
  }


  // CheckStyle:OFF
  public static class Builder extends DefaultConnectionFactory.Builder
  {

    private final SingleConnectionFactory object;


    protected Builder()
    {
      object = new SingleConnectionFactory();
    }


    protected Builder(final Transport transport)
    {
      object = new SingleConnectionFactory(transport);
    }


    public Builder config(final ConnectionConfig cc)
    {
      object.setConnectionConfig(cc);
      return this;
    }


    public Builder failFastInitialize(final boolean failFast)
    {
      object.setFailFastInitialize(failFast);
      return this;
    }


    public Builder nonBlockingInitialize(final boolean nonBlocking)
    {
      object.setNonBlockingInitialize(nonBlocking);
      return this;
    }


    public SingleConnectionFactory build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
