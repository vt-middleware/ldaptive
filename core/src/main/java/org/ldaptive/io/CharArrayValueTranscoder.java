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
 * Decodes and encodes a character array for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2994 $ $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
 */
public class CharArrayValueTranscoder
  extends AbstractStringValueTranscoder<char[]>
{


  /** {@inheritDoc} */
  @Override
  public char[] decodeStringValue(final String value)
  {
    return value.toCharArray();
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final char[] value)
  {
    return String.valueOf(value);
  }


  /** {@inheritDoc} */
  @Override
  public Class<char[]> getType()
  {
    return char[].class;
  }
}
