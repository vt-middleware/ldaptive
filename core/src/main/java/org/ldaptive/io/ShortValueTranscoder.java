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
package org.ldaptive.io;

/**
 * Decodes and encodes a short for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2886 $ $Date: 2014-02-26 12:21:59 -0500 (Wed, 26 Feb 2014) $
 */
public class ShortValueTranscoder
  extends AbstractPrimitiveValueTranscoder<Short>
{


  /** Default constructor. */
  public ShortValueTranscoder() {}


  /**
   * Creates a new short value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public ShortValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  /** {@inheritDoc} */
  @Override
  public Short decodeStringValue(final String value)
  {
    return Short.valueOf(value);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Short> getType()
  {
    return isPrimitive() ? short.class : Short.class;
  }
}
