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
 * Decodes and encodes a long for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public class LongValueTranscoder extends AbstractPrimitiveValueTranscoder<Long>
{


  /** Default constructor. */
  public LongValueTranscoder() {}


  /**
   * Creates a new long value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public LongValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  /** {@inheritDoc} */
  @Override
  public Long decodeStringValue(final String value)
  {
    return Long.valueOf(value);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Long> getType()
  {
    return isPrimitive() ? long.class : Long.class;
  }
}
