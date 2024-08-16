/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;

/**
 * Default implementation of a referral connection factory. Delegates to a {@link DefaultConnectionFactory}.
 *
 * @author  Middleware Services
 */
public class DefaultReferralConnectionFactory implements ReferralConnectionFactory
{

  /** Factory to copy properties from. */
  private final DefaultConnectionFactory factory;


  /**
   * Creates a new default referral connection factory.
   */
  public DefaultReferralConnectionFactory()
  {
    this(new ConnectionConfig());
  }


  /**
   * Creates a new default referral connection factory.
   *
   * @param  cc  connection configuration to copy properties from
   */
  public DefaultReferralConnectionFactory(final ConnectionConfig cc)
  {
    final ConnectionConfig config = ConnectionConfig.copy(cc);
    config.setLdapUrl(null);
    factory = DefaultConnectionFactory.builder().config(config).freeze().build();
  }


  /**
   * Creates a new default referral connection factory.
   *
   * @param  cf  default connection factory to copy properties from
   */
  public DefaultReferralConnectionFactory(final DefaultConnectionFactory cf)
  {
    factory = copy(cf, null);
  }


  /**
   * Creates a copy of the supplied connection factory and sets the supplied URL on the new connection factory.
   *
   * @param  cf  to copy
   * @param  url  to set on the new connection factory
   *
   * @return  default connection factory
   */
  private DefaultConnectionFactory copy(final DefaultConnectionFactory cf, final String url)
  {
    final DefaultConnectionFactory.Builder builder;
    if (cf.getTransport() != null) {
      builder = DefaultConnectionFactory.builder(cf.getTransport());
    } else {
      builder = DefaultConnectionFactory.builder();
    }
    final ConnectionConfig cc = ConnectionConfig.copy(cf.getConnectionConfig());
    cc.setLdapUrl(url);
    return builder.config(cc).freeze().build();
  }


  @Override
  public ConnectionFactory getConnectionFactory(final String url)
  {
    return copy(factory, url);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::factory=" + factory;
  }
}
