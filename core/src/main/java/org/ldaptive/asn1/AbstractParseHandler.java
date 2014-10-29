/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.asn1;

/**
 * Parse handler for managing and initializing an object.
 *
 * @param  <T>  type of object initialized by this handler
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractParseHandler<T> implements ParseHandler
{

  /** Object to initialize. */
  private final T object;


  /**
   * Creates a new abstract parse handler.
   *
   * @param  t  object to initialize
   */
  public AbstractParseHandler(final T t)
  {
    object = t;
  }


  /**
   * Returns the object.
   *
   * @return  object
   */
  public T getObject()
  {
    return object;
  }
}
