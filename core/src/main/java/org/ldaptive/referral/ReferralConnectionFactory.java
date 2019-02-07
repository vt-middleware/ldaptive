/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionFactory;

/**
 * Factory for creating connections used by referrals.
 *
 * @author  Middleware Services
 */
public interface ReferralConnectionFactory
{


  /**
   * Returns a connection factory for use with a referral.
   *
   * @param  url  LDAP URL to the referral server
   *
   * @return  connection factory
   */
  ConnectionFactory getConnectionFactory(String url);
}
