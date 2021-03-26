/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.ConnectionActivator;
import org.ldaptive.pool.ConnectionPassivator;
import org.ldaptive.pool.PoolException;
import org.ldaptive.pool.PruneStrategy;
import org.ldaptive.pool.ValidationException;
import org.ldaptive.pool.ValidationExceptionHandler;
import org.ldaptive.transport.Transport;
import org.ldaptive.transport.TransportFactory;

/**
 * Creates connections for performing ldap operations and manages those connections as a pool.
 *
 * @author  Middleware Services
 */
public class PooledConnectionFactory extends BlockingConnectionPool implements ConnectionFactory
{

  /** Validation exception handler. */
  private ValidationExceptionHandler validationExceptionHandler = new RetryValidationExceptionHandler();


  /** Default constructor. */
  public PooledConnectionFactory()
  {
    setDefaultConnectionFactory(
      new DefaultConnectionFactory(TransportFactory.getTransport(PooledConnectionFactory.class)));
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  t  transport
   */
  public PooledConnectionFactory(final Transport t)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(t));
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  ldapUrl  to connect to
   */
  public PooledConnectionFactory(final String ldapUrl)
  {
    setDefaultConnectionFactory(
      new DefaultConnectionFactory(ldapUrl, TransportFactory.getTransport(PooledConnectionFactory.class)));
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  ldapUrl  to connect to
   * @param  t  transport
   */
  public PooledConnectionFactory(final String ldapUrl, final Transport t)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(ldapUrl, t));
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  cc  connection configuration
   */
  public PooledConnectionFactory(final ConnectionConfig cc)
  {
    setDefaultConnectionFactory(
      new DefaultConnectionFactory(cc, TransportFactory.getTransport(PooledConnectionFactory.class)));
  }


  /**
   * Creates a new pooled connection factory.
   *
   * @param  cc  connection configuration
   * @param  t  transport
   */
  public PooledConnectionFactory(final ConnectionConfig cc, final Transport t)
  {
    setDefaultConnectionFactory(new DefaultConnectionFactory(cc, t));
  }


  @Override
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
   * Returns the validation exception handler.
   *
   * @return  validation exception handler
   */
  public ValidationExceptionHandler getValidationExceptionHandler()
  {
    return validationExceptionHandler;
  }


  /**
   * Sets the validation exception handler.
   *
   * @param  handler  validation exception handler
   */
  public void setValidationExceptionHandler(final ValidationExceptionHandler handler)
  {
    validationExceptionHandler = handler;
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
  public Connection getConnection()
    throws PoolException
  {
    try {
      return super.getConnection();
    } catch (ValidationException e) {
      if (validationExceptionHandler != null) {
        logger.warn("Connection could not be validated, invoking handler {}", validationExceptionHandler, e);
        final Connection conn = validationExceptionHandler.apply(e);
        if (conn != null) {
          return conn;
        }
      }
      throw e;
    }
  }


  @Override
  public void close()
  {
    super.close();
    getDefaultConnectionFactory().close();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      super.toString()).append(", ")
      .append("validationExceptionHandler=").append(validationExceptionHandler).append("]").toString();
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


    public Builder min(final int size)
    {
      object.setMinPoolSize(size);
      return this;
    }


    public Builder max(final int size)
    {
      object.setMaxPoolSize(size);
      return this;
    }


    public Builder validateOnCheckIn(final boolean b)
    {
      object.setValidateOnCheckIn(b);
      return this;
    }


    public Builder validateOnCheckOut(final boolean b)
    {
      object.setValidateOnCheckOut(b);
      return this;
    }


    public Builder validatePeriodically(final boolean b)
    {
      object.setValidatePeriodically(b);
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


    public Builder activator(final ConnectionActivator activator)
    {
      object.setActivator(activator);
      return this;
    }


    public Builder passivator(final ConnectionPassivator passivator)
    {
      object.setPassivator(passivator);
      return this;
    }


    public Builder validator(final ConnectionValidator validator)
    {
      object.setValidator(validator);
      return this;
    }


    public Builder validationExceptionHandler(final ValidationExceptionHandler handler)
    {
      object.setValidationExceptionHandler(handler);
      return this;
    }


    public Builder pruneStrategy(final PruneStrategy strategy)
    {
      object.setPruneStrategy(strategy);
      return this;
    }


    public Builder name(final String name)
    {
      object.setName(name);
      return this;
    }


    public PooledConnectionFactory build()
    {
      return object;
    }
  }
  // CheckStyle:ON


  /**
   * Validation exception handler that attempts to retrieve another connection. This implementation makes {@link
   * #getMaxPoolSize()} attempts before giving up.
   */
  public class RetryValidationExceptionHandler implements ValidationExceptionHandler
  {


    @Override
    public Connection apply(final ValidationException e)
    {
      // limit to max pool size since a validation exception has already occurred
      // this should guarantee you force at least one new connection to be created
      for (int i = 0; i < getMaxPoolSize(); i++) {
        try {
          return PooledConnectionFactory.super.getConnection();
        } catch (ValidationException ex) {
          logger.warn("Validation exception handler failed on retry {}", i, ex);
        } catch (Exception ex) {
          logger.warn("Validation exception handler failed", ex);
          break;
        }
      }
      return null;
    }
  }
}
