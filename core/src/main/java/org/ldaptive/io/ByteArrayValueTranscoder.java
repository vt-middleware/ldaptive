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
 * Decodes and encodes a byte array for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2994 $ $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
 */
public class ByteArrayValueTranscoder
  extends AbstractBinaryValueTranscoder<byte[]>
{


  /** {@inheritDoc} */
  @Override
  public byte[] decodeBinaryValue(final byte[] value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encodeBinaryValue(final byte[] value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public Class<byte[]> getType()
  {
    return byte[].class;
  }
}
