/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.control.ResponseControl;

/**
 * Exception thrown when a connection attempt fails.
 *
 * @author  Middleware Services
 */
public class ConnectionException extends LdapException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -1902801031167384619L;


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   */
  public ConnectionException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   */
  public ConnectionException(final String msg, final ResultCode code)
  {
    super(msg, code);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public ConnectionException(final String msg, final ResultCode code, final String dn)
  {
    super(msg, code, dn);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public ConnectionException(final String msg, final ResultCode code, final String dn, final ResponseControl[] c)
  {
    super(msg, code, dn, c);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public ConnectionException(
    final String msg,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(msg, code, dn, c, urls);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  e  provider specific exception
   */
  public ConnectionException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   */
  public ConnectionException(final Exception e, final ResultCode code)
  {
    super(e, code);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public ConnectionException(final Exception e, final ResultCode code, final String dn)
  {
    super(e, code, dn);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public ConnectionException(final Exception e, final ResultCode code, final String dn, final ResponseControl[] c)
  {
    super(e, code, dn, c);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public ConnectionException(
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(e, code, dn, c, urls);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   */
  public ConnectionException(final String msg, final Exception e)
  {
    super(msg, e);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   */
  public ConnectionException(final String msg, final Exception e, final ResultCode code)
  {
    super(msg, e, code);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public ConnectionException(final String msg, final Exception e, final ResultCode code, final String dn)
  {
    super(msg, e, code, dn);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public ConnectionException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c)
  {
    super(msg, e, code, dn, c);
  }


  /**
   * Creates a new connection exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public ConnectionException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(msg, e, code, dn, c, urls);
  }
}
