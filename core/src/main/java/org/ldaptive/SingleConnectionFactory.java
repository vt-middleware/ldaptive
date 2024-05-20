/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.ldaptive.transport.Transport;
import org.ldaptive.transport.TransportFactory;

/**
 * Creates a single connection which is proxied for LDAP operations.
 *
 * @author  Middleware Services
 */
public final class SingleConnectionFactory extends DefaultConnectionFactory
{

  /** The proxy used by this factory. */
  private ConnectionProxy proxy;

  /** Whether {@link #initialize()} has been successfully invoked. */
  private boolean initialized;

  /** Whether {@link #initialize()} should throw if the connection cannot be opened. */
  private boolean failFastInitialize = true;

  /** Whether {@link #initialize()} should occur on a separate thread. */
  private boolean nonBlockingInitialize;

  /** To run when a connection is opened. */
  private Function<Connection, Boolean> onOpen;

  /** To run when a connection is closed. */
  private Function<Connection, Boolean> onClose;

  /** For validating the connection. */
  private ConnectionValidator validator;

  /** Executor for scheduling factory tasks. */
  private ExecutorService factoryExecutor;


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


  @Override
  public void freeze()
  {
    super.freeze();
    freeze(validator);
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
    assertMutable();
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
    assertMutable();
    nonBlockingInitialize = b;
  }


  /**
   * Returns the function to run when the connection is opened.
   *
   * @return  on open function
   */
  public Function<Connection, Boolean> getOnOpen()
  {
    return onOpen;
  }


  /**
   * Sets the function to run when the connection is opened.
   *
   * @param  function  to run on connection open
   */
  public void setOnOpen(final Function<Connection, Boolean> function)
  {
    assertMutable();
    onOpen = function;
  }


  /**
   * Returns the function to run when the connection is closed.
   *
   * @return  on close function
   */
  public Function<Connection, Boolean> getOnClose()
  {
    return onClose;
  }


  /**
   * Sets the function to run when the connection is closed.
   *
   * @param  function  to run on connection close
   */
  public void setOnClose(final Function<Connection, Boolean> function)
  {
    assertMutable();
    onClose = function;
  }


  /**
   * Returns the connection validator for this factory.
   *
   * @return  connection validator
   */
  public ConnectionValidator getValidator()
  {
    return validator;
  }


  /**
   * Sets the connection validator for this factory.
   *
   * @param  cv  connection validator
   */
  public void setValidator(final ConnectionValidator cv)
  {
    assertMutable();
    validator = cv;
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
      throw new IllegalStateException("Connection factory is already initialized for " + this);
    }
    if (nonBlockingInitialize) {
      if (factoryExecutor == null) {
        if (validator != null) {
          factoryExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> {
              final Thread t = new Thread(r, "ldaptive-" + getClass().getSimpleName() + "@" + hashCode());
              t.setDaemon(true);
              return t;
            });
        } else {
          factoryExecutor = Executors.newCachedThreadPool(
            r -> {
              final Thread t = new Thread(r, "ldaptive-" + getClass().getSimpleName() + "@" + hashCode());
              t.setDaemon(true);
              return t;
            });
        }
      }
      factoryExecutor.execute(
        () -> {
          try {
            initializeInternal();
            logger.info("Initialize successful for {}", SingleConnectionFactory.this);
          } catch (LdapException e) {
            logger.debug("Initialize failed for {}", SingleConnectionFactory.this, e);
          }
        });
    } else {
      if (validator != null) {
        factoryExecutor = Executors.newSingleThreadScheduledExecutor(
          r -> {
            final Thread t = new Thread(r, "ldaptive-" + getClass().getSimpleName() + "@" + hashCode());
            t.setDaemon(true);
            return t;
          });
      }
      initializeInternal();
      logger.info("Initialize successful for {}", SingleConnectionFactory.this);
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
    LdapException initializeEx = null;
    try {
      initializeConnectionProxy();
      logger.info("Factory initialized {}", this);
    } catch (LdapException e) {
      initializeEx = e;
      logger.warn("Could not initialize connection factory for {}", SingleConnectionFactory.this, e);
    }
    if (validator != null) {
      ((ScheduledExecutorService) factoryExecutor).scheduleAtFixedRate(
        () -> {
          logger.debug("Begin validate task for {}", SingleConnectionFactory.this);
          try {
            validator.apply(proxy != null ? proxy.getConnection() : null);
          } catch (Exception e) {
            logger.warn("Validation task failed for {}", SingleConnectionFactory.this, e);
          }
          logger.debug("End validate task for {}", SingleConnectionFactory.this);
        },
        validator.getValidatePeriod().toMillis(),
        validator.getValidatePeriod().toMillis(),
        TimeUnit.MILLISECONDS);
    }
    if (initializeEx != null && failFastInitialize) {
      throw initializeEx;
    }
    this.freeze();
  }


  /**
   * Opens the connection and creates the connection proxy. Invokes {@link #onOpen} and will tear down the connection if
   * that function returns false.
   *
   * @throws  LdapException  if connection open fails
   */
  private synchronized void initializeConnectionProxy()
    throws LdapException
  {
    final Connection connection = super.getConnection();
    connection.open();
    proxy = new ConnectionProxy(connection);
    initialized = true;
    if (onOpen != null && !onOpen.apply(proxy.getConnection())) {
      connection.close();
      proxy = null;
      initialized = false;
      throw new LdapException("On open function failed for " + this);
    }
  }


  /**
   * Closes the connection and sets the proxy to null. Invokes {@link #onClose} prior to closing the connection.
   */
  private synchronized void destroyConnectionProxy()
  {
    if (proxy != null) {
      if (onClose != null && !onClose.apply(proxy.getConnection())) {
        logger.warn("On close function {} failed for {}", onClose, this);
      }
      proxy.getConnection().close();
    }
    proxy = null;
    initialized = false;
  }


  @Override
  public Connection getConnection()
  {
    if (!initialized) {
      throw new IllegalStateException("Connection factory is not initialized");
    }
    return (Connection) Proxy.newProxyInstance(
      Connection.class.getClassLoader(),
      new Class[] {Connection.class},
      proxy);
  }


  @Override
  public synchronized void close()
  {
    destroyConnectionProxy();
    super.close();
    if (factoryExecutor != null) {
      try {
        factoryExecutor.shutdown();
      } finally {
        factoryExecutor = null;
      }
    }
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "transport=" + getTransport() + ", " +
      "connection=" + (proxy != null ? proxy.getConnection() : null) + ", " +
      "failFastInitialize=" + failFastInitialize + ", " +
      "nonBlockingInitialize=" + nonBlockingInitialize + ", " +
      "onOpen=" + onOpen + ", " +
      "onClose=" + onClose + ", " +
      "validator=" + validator + ", " +
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


    /**
     * Returns the connection that is being proxied.
     *
     * @return  underlying connection
     */
    public Connection getConnection()
    {
      return conn;
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


    public Builder config(final ConnectionConfig config)
    {
      object.setConnectionConfig(config);
      return this;
    }


    public Builder onOpen(final Function<Connection, Boolean> function)
    {
      object.setOnOpen(function);
      return this;
    }


    public Builder onClose(final Function<Connection, Boolean> function)
    {
      object.setOnClose(function);
      return this;
    }


    public Builder validator(final ConnectionValidator validator)
    {
      object.setValidator(validator);
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


  /** Invokes {@link #destroyConnectionProxy()} followed by {@link #initializeConnectionProxy()}. */
  public class ReinitializeConnectionConsumer implements Consumer<Connection>
  {

    @Override
    public void accept(final Connection conn)
    {
      if (proxy != null && !proxy.getConnection().equals(conn)) {
        throw new IllegalArgumentException("Connection not managed by this factory: " + conn);
      }
      try {
        destroyConnectionProxy();
        initializeConnectionProxy();
      } catch (Exception e) {
        logger.error("Could not reinitialize the connection proxy", e);
      }
    }
  }
}
