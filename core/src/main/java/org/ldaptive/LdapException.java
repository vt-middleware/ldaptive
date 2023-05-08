/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Base exception for all ldap related exceptions.
 *
 * @author  Middleware Services
 */
public class LdapException extends Exception
{

  /** serialVersionUID. */
  private static final long serialVersionUID = 6812614366508784841L;

  /** Optional result code associated with this exception. */
  private final ResultCode resultCode;


  /**
   * Creates a new ldap exception based on the supplied result.
   *
   * @param  result  that produced this exception
   */
  public LdapException(final Result result)
  {
    this(result.getResultCode(), formatResult(result));
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   */
  public LdapException(final String msg)
  {
    this(null, msg);
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  code result code describing this exception
   * @param  msg  describing this exception
   */
  public LdapException(final ResultCode code, final String msg)
  {
    super(msg);
    resultCode = code;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  underlying exception
   */
  public LdapException(final Throwable e)
  {
    this((ResultCode) null, e);
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  code result code describing this exception
   * @param  e  underlying exception
   */
  public LdapException(final ResultCode code, final Throwable e)
  {
    super(e);
    resultCode = code;
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public LdapException(final String msg, final Throwable e)
  {
    this(null, msg, e);
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  code result code describing this exception
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public LdapException(final ResultCode code, final String msg, final Throwable e)
  {
    super(msg, e);
    resultCode = code;
  }


  /**
   * Returns the result code.
   *
   * @return  result code or null
   */
  public ResultCode getResultCode()
  {
    return resultCode;
  }


  /**
   * Formats the supplied result for use as an exception message.
   *
   * @param  result  to format
   *
   * @return  formatted result
   */
  protected static String formatResult(final Result result)
  {
    return "resultCode=" + result.getResultCode() + ", " + "diagnosticMessage=" + result.getEncodedDiagnosticMessage();
  }
}
