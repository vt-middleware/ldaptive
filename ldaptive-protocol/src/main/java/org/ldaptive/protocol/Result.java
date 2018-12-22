/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.ResultCode;

/**
 * LDAP protocol result.
 *
 * @author  Middleware Services
 */
public interface Result extends Message
{


  /**
   * Returns the result code.
   *
   * @return  result code
   */
  ResultCode getResultCode();


  /**
   * Returns the matched DN.
   *
   * @return  matched DN
   */
  String getMatchedDN();


  /**
   * Returns the diagnostic message.
   *
   * @return  diagnostic message
   */
  String getDiagnosticMessage();


  /**
   * Returns the referral URLs.
   *
   * @return  referral URLs
   */
  String[] getReferralURLs();
}
