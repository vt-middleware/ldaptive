/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.ResponseControl;

/**
 * Exception thrown when an ldap operation attempt fails.
 *
 * @author  Middleware Services
 */
public class OperationException extends LdapException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = 4995760197708755601L;


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   */
  public OperationException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   */
  public OperationException(final String msg, final ResultCode code)
  {
    super(msg, code);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public OperationException(final String msg, final ResultCode code, final String dn)
  {
    super(msg, code, dn);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public OperationException(final String msg, final ResultCode code, final String dn, final ResponseControl[] c)
  {
    super(msg, code, dn, c);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public OperationException(
    final String msg,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(msg, code, dn, c, urls);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  e  provider specific exception
   */
  public OperationException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   */
  public OperationException(final Exception e, final ResultCode code)
  {
    super(e, code);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public OperationException(final Exception e, final ResultCode code, final String dn)
  {
    super(e, code, dn);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public OperationException(final Exception e, final ResultCode code, final String dn, final ResponseControl[] c)
  {
    super(e, code, dn, c);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public OperationException(
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c,
    final String[] urls)
  {
    super(e, code, dn, c, urls);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   */
  public OperationException(final String msg, final Exception e)
  {
    super(msg, e);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   */
  public OperationException(final String msg, final Exception e, final ResultCode code)
  {
    super(msg, e, code);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   */
  public OperationException(final String msg, final Exception e, final ResultCode code, final String dn)
  {
    super(msg, e, code, dn);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   */
  public OperationException(
    final String msg,
    final Exception e,
    final ResultCode code,
    final String dn,
    final ResponseControl[] c)
  {
    super(msg, e, code, dn, c);
  }


  /**
   * Creates a new operation exception.
   *
   * @param  msg  describing this exception
   * @param  e  provider specific exception
   * @param  code  result code
   * @param  dn  matched dn
   * @param  c  response controls
   * @param  urls  referral urls
   */
  public OperationException(
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
