/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.transport.Transport;
import org.ldaptive.transport.TransportFactory;
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

  /** Transport used by this factory. */
  private Transport transport;

  /** Connection configuration used by this factory. */
  private ConnectionConfig config;


  /** Default constructor. */
  public DefaultConnectionFactory()
  {
    this(TransportFactory.getTransport(DefaultConnectionFactory.class));
  }


  /**
   * Creates a new default connection factory. Be sure to invoke {@link #close()} if the supplied transport has
   * resources to cleanup.
   *
   * @param  t  transport
   */
  public DefaultConnectionFactory(final Transport t)
  {
    transport = t;
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
   * Creates a new default connection factory. Be sure to invoke {@link #close()} if the supplied transport has
   * resources to cleanup.
   *
   * @param  ldapUrl  to connect to
   * @param  t  transport
   */
  public DefaultConnectionFactory(final String ldapUrl, final Transport t)
  {
    this(new ConnectionConfig(ldapUrl), t);
  }


  /**
   * Creates a new default connection factory.
   *
   * @param  cc  connection configuration
   */
  public DefaultConnectionFactory(final ConnectionConfig cc)
  {
    this(cc, TransportFactory.getTransport(DefaultConnectionFactory.class));
  }


  /**
   * Creates a new default connection factory. Be sure to invoke {@link #close()} if the supplied transport has
   * resources to cleanup.
   *
   * @param  cc  connection configuration
   * @param  t  transport
   */
  public DefaultConnectionFactory(final ConnectionConfig cc, final Transport t)
  {
    transport = t;
    setConnectionConfig(cc);
  }


  @Override
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
   * Returns the ldap transport.
   *
   * @return  ldap transport
   */
  public Transport getTransport()
  {
    return transport;
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
    return transport.create(config);
  }


  @Override
  public void close()
  {
    transport.close();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("transport=").append(transport).append(", ")
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
