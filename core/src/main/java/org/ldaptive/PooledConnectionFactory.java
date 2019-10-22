/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.ldaptive.pool.Activator;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.Passivator;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PruneStrategy;
import org.ldaptive.pool.Validator;
import org.ldaptive.transport.Transport;

/**
 * Creates connections for performing ldap operations.
 *
 * @author  Middleware Services
 */
public class PooledConnectionFactory extends BlockingConnectionPool implements ConnectionFactory
{


  /** Default constructor. */
  public PooledConnectionFactory()
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory());
    setPoolConfig(new PoolConfig());
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  ldapUrl  to connect to
   */
  public PooledConnectionFactory(final String ldapUrl)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(ldapUrl));
    setPoolConfig(new PoolConfig());
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  cc  connection configuration
   */
  public PooledConnectionFactory(final ConnectionConfig cc)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(cc));
    setPoolConfig(new PoolConfig());
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  cc  connection configuration
   * @param  pc  pool configuration
   */
  public PooledConnectionFactory(final ConnectionConfig cc, final PoolConfig pc)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(cc));
    setPoolConfig(pc);
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  cc  connection configuration
   * @param  pc  pool configuration
   * @param  t  transport
   */
  public PooledConnectionFactory(final ConnectionConfig cc, final PoolConfig pc, final Transport t)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(cc, t));
    setPoolConfig(pc);
  }


  /**
   * Returns the connection config.
   *
   * @return  connection config
   */
  public ConnectionConfig getConnectionConfig()
  {
    return getDefaultConnectionFactory().getConnectionConfig();
  }


  /**
   * Sets the connection config. Once invoked the supplied connection config is made immutable. See {@link
   * ConnectionConfig#makeImmutable()}.
   *
   * @param  cc  connection config
   */
  public void setConnectionConfig(final ConnectionConfig cc)
  {
    getDefaultConnectionFactory().setConnectionConfig(cc);
  }


  /**
   * Returns the ldap transport.
   *
   * @return  ldap transport
   */
  public Transport getTransport()
  {
    return getDefaultConnectionFactory().getTransport();
  }


  @Override
  public void close()
  {
    super.close();
    getDefaultConnectionFactory().close();
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

    private final PooledConnectionFactory object = new PooledConnectionFactory();


    protected Builder() {}


    public Builder config(final ConnectionConfig cc)
    {
      object.setConnectionConfig(cc);
      return this;
    }


    public Builder config(final PoolConfig pc)
    {
      object.setPoolConfig(pc);
      return this;
    }


    public Builder blockWaitTime(final Duration time)
    {
      object.setBlockWaitTime(time);
      return this;
    }


    public Builder connectOnCreate(final boolean connect)
    {
      object.setConnectOnCreate(connect);
      return this;
    }


    public Builder failFastInitialize(final boolean failFast)
    {
      object.setFailFastInitialize(failFast);
      return this;
    }


    public Builder activator(final Activator<Connection> activator)
    {
      object.setActivator(activator);
      return this;
    }


    public Builder passivator(final Passivator<Connection> passivator)
    {
      object.setPassivator(passivator);
      return this;
    }


    public Builder validator(final Validator<Connection> validator)
    {
      object.setValidator(validator);
      return this;
    }


    public Builder pruneStrategy(final PruneStrategy strategy)
    {
      object.setPruneStrategy(strategy);
      return this;
    }


    public PooledConnectionFactory build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
