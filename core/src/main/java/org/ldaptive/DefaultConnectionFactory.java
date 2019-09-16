/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates connections for performing ldap operations.
 *
 * @author  Middleware Services
 */
public class DefaultConnectionFactory implements ConnectionFactory
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Provider used by this factory. */
  private Provider provider;

  /** Connection configuration used by this factory. */
  private ConnectionConfig config;


  /** Default constructor. */
  public DefaultConnectionFactory()
  {
    provider = ProviderFactory.getProvider();
  }


  /**
   * Creates a new default connection factory.
   *
   * @param  ldapUrl  to connect to
   */
  public DefaultConnectionFactory(final String ldapUrl)
  {
    this(new ConnectionConfig(ldapUrl));
  }


  /**
   * Creates a new default connection factory.
   *
   * @param  cc  connection configuration
   */
  public DefaultConnectionFactory(final ConnectionConfig cc)
  {
    this(cc, ProviderFactory.getProvider());
  }


  /**
   * Creates a new default connection factory.
   *
   * @param  cc  connection configuration
   * @param  p  provider
   */
  public DefaultConnectionFactory(final ConnectionConfig cc, final Provider p)
  {
    provider = p;
    setConnectionConfig(cc);
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
   * Sets the connection config. Once invoked the supplied connection config is made immutable. See {@link
   * ConnectionConfig#makeImmutable()}.
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
  public Provider getProvider()
  {
    return provider;
  }


  /**
   * Creates a new connection. Connections returned from this method must be opened before they can perform ldap
   * operations.
   *
   * @return  connection
   */
  @Override
  public Connection getConnection()
  {
    return provider.create(config);
  }


  @Override
  public void close()
  {
    provider.close();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("provider=").append(provider).append(", ")
      .append("config=").append(config).append("]").toString();
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
  public static class Builder
  {


    private final DefaultConnectionFactory object = new DefaultConnectionFactory();


    protected Builder() {}


    public Builder config(final ConnectionConfig cc)
    {
      object.setConnectionConfig(cc);
      return this;
    }


    public DefaultConnectionFactory build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
