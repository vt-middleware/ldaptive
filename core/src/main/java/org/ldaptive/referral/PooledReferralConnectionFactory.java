/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.PooledConnectionFactory;

/**
 * Pooled implementation of a referral connection factory. Stores a map of connection URLs to {@link
 * PooledConnectionFactory}. Note that this class is intended to hold references to multiple connection pools until it
 * is closed.
 *
 * @author  Middleware Services
 */
public class PooledReferralConnectionFactory implements ReferralConnectionFactory
{

  /** Map of connection URL to connection factories. */
  private final Map<String, PooledConnectionFactory> factories = new HashMap<>();

  /** Factory to copy properties from. */
  private final PooledConnectionFactory factory;

  /** Whether this connection factory has been closed. */
  private boolean closed;


  /**
   * Creates a new pooled referral connection factory.
   *
   * @param  cf  pooled connection factory to copy properties from
   */
  public PooledReferralConnectionFactory(final PooledConnectionFactory cf)
  {
    factory = copy(cf, null);
  }


  /**
   * Creates a new instance of the supplied connection factory with the same settings.
   *
   * @param  cf  to copy
   * @param  url  to set in the connection configuration
   *
   * @return  new pooled connection factory
   */
  private PooledConnectionFactory copy(final PooledConnectionFactory cf, final String url)
  {
    final PooledConnectionFactory.Builder builder;
    if (cf.getTransport() != null) {
      builder = PooledConnectionFactory.builder(cf.getTransport());
    } else {
      builder = PooledConnectionFactory.builder();
    }
    final ConnectionConfig cc = ConnectionConfig.copy(cf.getConnectionConfig());
    cc.setLdapUrl(url);
    return builder
      .config(cc)
      .connectOnCreate(cf.getConnectOnCreate())
      .activator(cf.getActivator())
      .passivator(cf.getPassivator())
      .pruneStrategy(cf.getPruneStrategy())
      .validator(cf.getValidator())
      .validationExceptionHandler(cf.getValidationExceptionHandler())
      .validateOnCheckIn(cf.isValidateOnCheckIn())
      .validateOnCheckOut(cf.isValidateOnCheckOut())
      .validatePeriodically(cf.isValidatePeriodically())
      .blockWaitTime(cf.getBlockWaitTime())
      .failFastInitialize(cf.getFailFastInitialize())
      .min(cf.getMinPoolSize())
      .max(cf.getMaxPoolSize())
      .name(cf.getName())
      .freeze()
      .build();
  }


  @Override
  public ConnectionFactory getConnectionFactory(final String url)
  {
    synchronized (factories) {
      if (closed) {
        throw new IllegalStateException("Connection factory is closed");
      }
      PooledConnectionFactory cf = factories.get(url);
      if (cf == null) {
        cf = copy(factory, url);
        cf.initialize();
        factories.put(url, cf);
      }
      return cf;
    }
  }


  /** Closes all the connection pools for this referral connection factory. */
  public void close()
  {
    synchronized (factories) {
      for (PooledConnectionFactory cf : factories.values()) {
        cf.close();
      }
      closed = true;
    }
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "factories=" + factories + ", " +
      "factory=" + factory + ", " +
      "closed=" + closed;
  }
}
