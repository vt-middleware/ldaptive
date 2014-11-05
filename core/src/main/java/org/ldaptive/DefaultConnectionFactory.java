/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderConnection;
import org.ldaptive.provider.ProviderConnectionFactory;
import org.ldaptive.provider.jndi.JndiProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates connections for performing ldap operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DefaultConnectionFactory implements ConnectionFactory
{

  /** Ldap provider class name. */
  public static final String PROVIDER = "org.ldaptive.provider";

  /** Static reference to the default ldap provider. */
  protected static final Provider<?> DEFAULT_PROVIDER = getDefaultProvider();

  /** Provider used by this factory. */
  private Provider<?> provider = DEFAULT_PROVIDER.newInstance();

  /** Connection configuration used by this factory. */
  private ConnectionConfig config;


  /** Default constructor. */
  public DefaultConnectionFactory() {}


  /**
   * Creates a new default connection factory.
   *
   * @param  ldapUrl  to connect to
   */
  public DefaultConnectionFactory(final String ldapUrl)
  {
    setConnectionConfig(new ConnectionConfig(ldapUrl));
  }


  /**
   * Creates a new default connection factory.
   *
   * @param  cc  connection configuration
   */
  public DefaultConnectionFactory(final ConnectionConfig cc)
  {
    setConnectionConfig(cc);
  }


  /**
   * Creates a new default connection factory.
   *
   * @param  cc  connection configuration
   * @param  p  provider
   */
  public DefaultConnectionFactory(
    final ConnectionConfig cc,
    final Provider<?> p)
  {
    setConnectionConfig(cc);
    setProvider(p);
  }


  /**
   * Returns the connection config.
   *
   * @return  connection config
   */
  public ConnectionConfig getConnectionConfig()
  {
    return config;
  }


  /**
   * Sets the connection config. Once invoked the supplied connection config is
   * made immutable. See {@link ConnectionConfig#makeImmutable()}.
   *
   * @param  cc  connection config
   */
  public void setConnectionConfig(final ConnectionConfig cc)
  {
    config = cc;
    config.makeImmutable();
  }


  /**
   * Returns the ldap provider.
   *
   * @return  ldap provider
   */
  public Provider<?> getProvider()
  {
    return provider;
  }


  /**
   * Sets the ldap provider.
   *
   * @param  p  ldap provider to set
   */
  public void setProvider(final Provider<?> p)
  {
    provider = p;
  }


  /**
   * Creates a new connection. Connections returned from this method must be
   * opened before they can perform ldap operations.
   *
   * @return  connection
   */
  @Override
  public Connection getConnection()
  {
    return new DefaultConnection(config, provider.getConnectionFactory(config));
  }


  /**
   * Creates a new connection. Connections returned from this method must be
   * opened before they can be used.
   *
   * @param  ldapUrl  to connect to
   *
   * @return  connection
   */
  public static Connection getConnection(final String ldapUrl)
  {
    final Provider<?> p = DEFAULT_PROVIDER.newInstance();
    final ConnectionConfig cc = new ConnectionConfig(ldapUrl);
    cc.makeImmutable();
    return new DefaultConnection(cc, p.getConnectionFactory(cc));
  }


  /**
   * Creates a new connection. Connections returned from this method must be
   * opened before they can be used.
   *
   * @param  cc  connection configuration
   *
   * @return  connection
   */
  public static Connection getConnection(final ConnectionConfig cc)
  {
    final Provider<?> p = DEFAULT_PROVIDER.newInstance();
    cc.makeImmutable();
    return new DefaultConnection(cc, p.getConnectionFactory(cc));
  }


  /**
   * The {@link #PROVIDER} property is checked and that class is loaded if
   * provided. Otherwise the JNDI provider is returned.
   *
   * @return  default provider
   */
  public static Provider<?> getDefaultProvider()
  {
    Provider<?> p;
    final String providerClass = System.getProperty(PROVIDER);
    if (providerClass != null) {
      final Logger l = LoggerFactory.getLogger(DefaultConnectionFactory.class);
      try {
        l.info("Setting ldap provider to {}", providerClass);
        p = (Provider<?>) Class.forName(providerClass).newInstance();
      } catch (Exception e) {
        l.error("Error instantiating {}", providerClass, e);
        throw new IllegalStateException(e);
      }
    } else {
      // set the default ldap provider to JNDI
      p = new JndiProvider();
    }
    return p;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::provider=%s, config=%s]",
        getClass().getName(),
        hashCode(),
        provider,
        config);
  }


  /** Default implementation for managing a connection to an LDAP. */
  protected static class DefaultConnection implements Connection
  {

    /** Logger for this class. */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /** Connection configuration. */
    private final ConnectionConfig config;

    /** Connection factory. */
    private final ProviderConnectionFactory<?> providerConnectionFactory;

    /** Provider connection. */
    private ProviderConnection providerConnection;


    /**
     * Creates a new default connection.
     *
     * @param  cc  connection configuration
     * @param  cf  provider connection factory
     */
    public DefaultConnection(
      final ConnectionConfig cc,
      final ProviderConnectionFactory<?> cf)
    {
      config = cc;
      providerConnectionFactory = cf;
    }


    /** {@inheritDoc} */
    @Override
    public ConnectionConfig getConnectionConfig()
    {
      return config;
    }


    /**
     * Returns the provider specific connection. Must be called after a
     * successful call to {@link #open()}.
     *
     * @return  provider connection
     *
     * @throws  IllegalStateException  if the connection is not open
     */
    @Override
    public ProviderConnection getProviderConnection()
    {
      if (!isOpen()) {
        throw new IllegalStateException("Connection is not open");
      }
      return providerConnection;
    }


    /**
     * This will establish a connection if one does not already exist. This
     * connection should be closed using {@link #close()}.
     *
     * @return  response associated with the {@link ConnectionInitializer} or an
     * empty response if no connection initializer was configured
     *
     * @throws  IllegalStateException  if the connection is already open
     * @throws  LdapException  if the LDAP cannot be reached
     */
    @Override
    public synchronized Response<Void> open()
      throws LdapException
    {
      if (isOpen()) {
        throw new IllegalStateException("Connection already open");
      }
      providerConnection = providerConnectionFactory.create();
      if (config.getConnectionInitializer() != null) {
        return config.getConnectionInitializer().initialize(this);
      } else {
        return new Response<>(null, null);
      }
    }


    /**
     * This will establish a connection if one does not already exist and bind
     * to the LDAP using the supplied bind request. This connection should be
     * closed using {@link #close()}.
     *
     * @param  request  bind request
     *
     * @return  response associated with the bind operation
     *
     * @throws  IllegalStateException  if the connection is already open
     * @throws  LdapException  if the LDAP cannot be reached
     */
    @Override
    public synchronized Response<Void> open(final BindRequest request)
      throws LdapException
    {
      if (isOpen()) {
        throw new IllegalStateException("Connection already open");
      }
      providerConnection = providerConnectionFactory.create();
      return providerConnection.bind(request);
    }


    /**
     * Returns whether the underlying provider connection is not null.
     *
     * @return  whether the provider connection has been initialized
     */
    @Override
    public boolean isOpen()
    {
      return providerConnection != null;
    }


    /** {@inheritDoc} */
    @Override
    public synchronized void close()
    {
      try {
        if (isOpen()) {
          providerConnection.close(null);
        }
      } catch (LdapException e) {
        logger.warn("Error closing connection with the LDAP", e);
      } finally {
        providerConnection = null;
      }
    }


    /** {@inheritDoc} */
    @Override
    public synchronized void close(final RequestControl[] controls)
    {
      try {
        if (isOpen()) {
          providerConnection.close(controls);
        }
      } catch (LdapException e) {
        logger.warn("Error closing connection with the LDAP", e);
      } finally {
        providerConnection = null;
      }
    }


    /** {@inheritDoc} */
    @Override
    public synchronized Response<Void> reopen()
      throws LdapException
    {
      try {
        if (providerConnection != null) {
          providerConnection.close(null);
        }
      } catch (LdapException e) {
        logger.warn("Error closing connection with the LDAP", e);
      } finally {
        providerConnection = null;
      }

      providerConnection = providerConnectionFactory.create();
      if (config.getConnectionInitializer() != null) {
        return config.getConnectionInitializer().initialize(this);
      } else {
        return new Response<>(null, null);
      }
    }


    /** {@inheritDoc} */
    @Override
    public synchronized Response<Void> reopen(final BindRequest request)
      throws LdapException
    {
      try {
        if (providerConnection != null) {
          providerConnection.close(null);
        }
      } catch (LdapException e) {
        logger.warn("Error closing connection with the LDAP", e);
      } finally {
        providerConnection = null;
      }

      providerConnection = providerConnectionFactory.create();
      return providerConnection.bind(request);
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::config=%s, providerConnectionFactory=%s, " +
          "providerConnection=%s]",
          getClass().getName(),
          hashCode(),
          config,
          providerConnectionFactory,
          providerConnection);
    }


    /** {@inheritDoc} */
    @Override
    protected void finalize()
      throws Throwable
    {
      try {
        close();
      } finally {
        super.finalize();
      }
    }
  }
}
