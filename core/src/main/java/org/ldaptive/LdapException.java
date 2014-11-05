/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.control.ResponseControl;

/**
 * Base exception for all ldap related exceptions. Provider specific exception
 * can be found using {@link #getCause()}.
 *
 * @author  Middleware Services
 */
public class LdapException extends Exception implements ResponseMessage
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -1283840230801970620L;

  /** ldap result code. */
  private final ResultCode resultCode;

  /** response matched DN. */
  private final String matchedDn;

  /** response controls. */
  private final ResponseControl[] responseControls;

  /** referral URLs. */
  private final String[] referralURLs;

  /** Message ID. */
  private final int messageId;


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   */
  public LdapException(final String msg)
  {
    super(msg);
    resultCode = null;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   */
  public LdapException(final String msg, final ResultCode code)
  {
    super(msg);
    resultCode = code;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public LdapException(final String msg, final ResultCode code, final String dn)
  {
    super(msg);
    resultCode = code;
    matchedDn = dn;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public LdapException(
    final String msg,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c)
  {
    super(msg);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public LdapException(
    final String msg,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(msg);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   * @param  id  message id
   */
  public LdapException(
    final String msg,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls,
    final int id)
  {
    super(msg);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = id;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  provider specific exception
   */
  public LdapException(final Exception e)
  {
    super(e);
    resultCode = null;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   */
  public LdapException(final Exception e, final ResultCode code)
  {
    super(e);
    resultCode = code;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public LdapException(
    final Exception e,
    final ResultCode code,
    final String dn)
  {
    super(e);
    resultCode = code;
    matchedDn = dn;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public LdapException(
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c)
  {
    super(e);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public LdapException(
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(e);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   * @param  id  message id
   */
  public LdapException(
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls,
    final int id)
  {
    super(e);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = id;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   */
  public LdapException(final String msg, final Exception e)
  {
    super(msg, e);
    resultCode = null;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   */
  public LdapException(
    final String msg,
    final Exception e,
    final ResultCode code)
  {
    super(msg, e);
    resultCode = code;
    matchedDn = null;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public LdapException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn)
  {
    super(msg, e);
    resultCode = code;
    matchedDn = dn;
    responseControls = null;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public LdapException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c)
  {
    super(msg, e);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = null;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public LdapException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(msg, e);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = -1;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   * @param  id  message id
   */
  public LdapException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls,
    final int id)
  {
    super(msg, e);
    resultCode = code;
    matchedDn = dn;
    responseControls = c;
    referralURLs = urls;
    messageId = id;
  }


  /**
   * Returns the ldap result code associated with this exception. May be null if
   * the provider did not set this value or could not determine this value.
   *
   * @return  ldap result code
   */
  public ResultCode getResultCode()
  {
    return resultCode;
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
        "[%s@%d::resultCode=%s, matchedDn=%s, responseControls=%s, " +
        "referralURLs=%s, messageId=%s, providerException=%s]",
        getClass().getName(),
        hashCode(),
        resultCode,
        matchedDn,
        Arrays.toString(responseControls),
        Arrays.toString(referralURLs),
        messageId,
        getCause());
  }
}
