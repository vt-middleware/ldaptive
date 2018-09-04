/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.control.ResponseControl;

/**
 * Wrapper class for all operation responses.
 *
 * @param  <T>  type of ldap result contained in this response
 *
 * @author  Middleware Services
 */
public class Response<T> implements ResponseMessage
{

  /** Property to configure the encoding of control characters in the response message. */
  public static final String ENCODE_CNTRL_CHARS = "org.ldaptive.response.encodeCntrlChars";

  /** Whether to encode control characters. */
  protected static boolean encodeCntrlChars;

  /**
   * statically initialize encoding of control characters.
   */
  static {
    final String ecc = System.getProperty(ENCODE_CNTRL_CHARS);
    if (ecc != null) {
      encodeCntrlChars = Boolean.valueOf(ecc);
    }
  }

  /** Operation response. */
  private final T result;

  /** Operation result code. */
  private final ResultCode resultCode;

  /** Response message. */
  private final String message;

  /** Response matched DN. */
  private final String matchedDn;

  /** Response controls. */
  private final ResponseControl[] responseControls;

  /** Referral URLs. */
  private final String[] referralURLs;

  /** Message ID. */
  private final int messageId;


  /**
   * Creates a new ldap response.
   *
   * @param  t  response type
   * @param  rc  result code
   */
  public Response(final T t, final ResultCode rc)
  {
    result = t;
    resultCode = rc;
    message = null;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap response.
   *
   * @param  t  response type
   * @param  rc  result code
   * @param  msg  result message
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   * @param  id  message id
   */
  public Response(
    final T t,
    final ResultCode rc,
    final String msg,
    final String dn,
    final ResponseControl[] c,
    final String[] urls,
    final int id)
  {
    result = t;
    resultCode = rc;
    message = msg;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = id;
  }


  /**
   * Returns the result of the ldap operation.
   *
   * @return  operation result
   */
  public T getResult()
  {
    return result;
  }


  /**
   * Returns the result code of the ldap operation.
   *
   * @return  operation result code
   */
  public ResultCode getResultCode()
  {
    return resultCode;
  }


  /**
   * Returns any error or diagnostic message produced by the ldap operation.
   *
   * @return  message
   */
  public String getMessage()
  {
    return message;
  }


  /**
   * Returns the matched DN produced by the ldap operation.
   *
   * @return  matched DN
   */
  public String getMatchedDn()
  {
    return matchedDn;
  }


  @Override
  public ResponseControl[] getControls()
  {
    return responseControls;
  }


  @Override
  public ResponseControl getControl(final String oid)
  {
    if (getControls() != null) {
      for (ResponseControl c : getControls()) {
        if (c.getOID().equals(oid)) {
          return c;
        }
      }
    }
    return null;
  }


  /**
   * Returns the referral URLs produced by the ldap operation.
   *
   * @return  referral urls
   */
  public String[] getReferralURLs()
  {
    return referralURLs;
  }


  @Override
  public int getMessageId()
  {
    return messageId;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::result=%s, resultCode=%s, message=%s, matchedDn=%s, " +
        "responseControls=%s, referralURLs=%s, messageId=%s]",
        getClass().getName(),
        hashCode(),
        result,
        resultCode,
        encodeCntrlChars ? LdapUtils.percentEncodeControlChars(message) : message,
        matchedDn,
        Arrays.toString(responseControls),
        Arrays.toString(referralURLs),
        messageId);
  }
}
