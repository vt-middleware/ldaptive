/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;

/**
 * Exception that indicates an invalid filter string.
 *
 * @author  Middleware Services
 */
public class FilterParseException extends LdapException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = 2314015446271971772L;


  /**
   * Creates a new filter parse exception.
   *
   * @param  code result code describing this exception
   * @param  msg  describing this exception
   */
  public FilterParseException(final ResultCode code, final String msg)
  {
    super(code, msg);
  }


  /**
   * Creates a new filter parse exception.
   *
   * @param  code result code describing this exception
   * @param  e  underlying exception
   */
  public FilterParseException(final ResultCode code, final Throwable e)
  {
    super(code, e);
  }


  /**
   * Creates a new filter parse exception.
   *
   * @param  code result code describing this exception
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public FilterParseException(final ResultCode code, final String msg, final Throwable e)
  {
    super(code, msg, e);
  }
}
