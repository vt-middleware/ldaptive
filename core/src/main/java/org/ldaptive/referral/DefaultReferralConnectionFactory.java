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

  /** Connection config for referrals. */
  private final ConnectionConfig connectionConfig;


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
   * @param  config  connection configuration
   */
  public DefaultReferralConnectionFactory(final ConnectionConfig config)
  {
    connectionConfig = config;
  }


  @Override
  public ConnectionFactory getConnectionFactory(final String url)
  {
    final ConnectionConfig cc = ConnectionConfig.copy(connectionConfig);
    cc.setLdapUrl(url);
    return DefaultConnectionFactory.builder().config(cc).makeImmutable().build();
  }
}
