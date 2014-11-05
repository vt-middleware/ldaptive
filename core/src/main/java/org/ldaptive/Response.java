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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class Response<T> implements ResponseMessage
{

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


  /** {@inheritDoc} */
  @Override
  public ResponseControl[] getControls()
  {
    return responseControls;
  }


  /** {@inheritDoc} */
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


  /** {@inheritDoc} */
  @Override
  public int getMessageId()
  {
    return messageId;
  }


  /** {@inheritDoc} */
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
        message,
        matchedDn,
        Arrays.toString(responseControls),
        Arrays.toString(referralURLs),
        messageId);
  }
}
