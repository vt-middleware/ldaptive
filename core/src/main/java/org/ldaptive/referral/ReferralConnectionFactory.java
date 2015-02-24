/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;

/**
 * @author  Middleware Services
 */
public interface ReferralConnectionFactory
{


  /**
   * Returns a connection factory for use with a referral.
   *
   * @param  config  for the connection factory
   * @param  ldapUrl  of the referred host
   *
   * @return  connection factory
   */
  ConnectionFactory getConnectionFactory(
    ConnectionConfig config,
    String ldapUrl);
}
