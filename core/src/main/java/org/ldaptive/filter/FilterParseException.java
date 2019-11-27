/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.LdapException;

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
   * @param  msg  describing this exception
   */
  public FilterParseException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new filter parse exception.
   *
   * @param  e  underlying exception
   */
  public FilterParseException(final Throwable e)
  {
    super(e);
  }


  /**
   * Creates a new filter parse exception.
   *
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public FilterParseException(final String msg, final Throwable e)
  {
    super(msg, e);
  }
}
