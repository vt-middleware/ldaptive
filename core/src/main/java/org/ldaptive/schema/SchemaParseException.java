/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import org.ldaptive.LdapException;

/**
 * Exception that indicates a schema element string could not be parsed.
 *
 * @author  Middleware Services
 */
public class SchemaParseException extends LdapException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -5214120370570326233L;


  /**
   * Creates a new schema parse exception.
   *
   * @param  msg  describing this exception
   */
  public SchemaParseException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new schema parse exception.
   *
   * @param  e  underlying exception
   */
  public SchemaParseException(final Throwable e)
  {
    super(e);
  }


  /**
   * Creates a new schema parse exception.
   *
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public SchemaParseException(final String msg, final Throwable e)
  {
    super(msg, e);
  }
}
