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
 * Decodes and encodes a double for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2886 $ $Date: 2014-02-26 12:21:59 -0500 (Wed, 26 Feb 2014) $
 */
public class DoubleValueTranscoder
  extends AbstractPrimitiveValueTranscoder<Double>
{


  /** Default constructor. */
  public DoubleValueTranscoder() {}


  /**
   * Creates a new double value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public DoubleValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  /** {@inheritDoc} */
  @Override
  public Double decodeStringValue(final String value)
  {
    return Double.valueOf(value);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Double> getType()
  {
    return isPrimitive() ? double.class : Double.class;
  }
}
