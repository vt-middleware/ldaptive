/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
import org.ldaptive.control.ResponseControl;

/**
 * LDAP protocol result.
 *
 * @author  Middleware Services
 */
public interface Result extends Message
{

  /** Whether to encode control characters. */
  boolean ENCODE_CNTRL_CHARS = Boolean.parseBoolean(
    System.getProperty("org.ldaptive.response.ENCODE_CNTRL_CHARS", "false"));


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
   * Returns the diagnostic message percent encoded if {@link #ENCODE_CNTRL_CHARS} is true. See {@link
   * LdapUtils#percentEncodeControlChars(String)}.
   *
   * @return  encoded message
   */
  default String getEncodedDiagnosticMessage()
  {
    if (getDiagnosticMessage() != null &&
        !"".equals(getDiagnosticMessage()) &&
        Result.ENCODE_CNTRL_CHARS) {
      return LdapUtils.percentEncodeControlChars(getDiagnosticMessage());
    } else {
      return getDiagnosticMessage();
    }
  }

  /** Result builder interface. */
  interface Builder<T extends Result> extends Message.Builder<T>
  {
    void resultCode(ResultCode resultCode);

    void diagnosticMessage(String diagnosticMessage);

    void matchedDN(String matchedDN);

    void referralURLs(List<String> urls);

    void referralURLs(String... urls);
  }
}
