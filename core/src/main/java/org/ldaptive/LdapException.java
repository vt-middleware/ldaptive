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


  /**
   * Creates a new ldap exception based on the supplied result.
   *
   * @param  result  that produced this exception
   */
  public LdapException(final Result result)
  {
    this(formatResult(result));
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   */
  public LdapException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  e  underlying exception
   */
  public LdapException(final Throwable e)
  {
    super(e);
  }


  /**
   * Creates a new ldap exception.
   *
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public LdapException(final String msg, final Throwable e)
  {
    super(msg, e);
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
    return new StringBuilder("resultCode=").append(result.getResultCode()).append(", ")
      .append("diagnosticMessage=").append(result.getEncodedDiagnosticMessage()).toString();
  }
}
