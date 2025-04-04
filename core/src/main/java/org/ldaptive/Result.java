/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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


  /**
   * Returns whether the result code in this result is {@link ResultCode#SUCCESS}.
   *
   * @return  whether this result is success
   */
  default boolean isSuccess()
  {
    return ResultCode.SUCCESS == getResultCode();
  }


  /**
   * Returns the diagnostic message with special characters encoded. See
   * {@link LdapUtils#percentEncodeControlChars(String)}.
   *
   * @return  encoded message
   */
  default String getEncodedDiagnosticMessage()
  {
    final String msg = getDiagnosticMessage();
    if (msg != null && !msg.isEmpty()) {
      return LdapUtils.percentEncodeControlChars(msg);
    }
    return msg;
  }
}
