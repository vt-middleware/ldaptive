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

import org.ldaptive.LdapUtils;

/**
 * Decodes and encodes an object for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2886 $ $Date: 2014-02-26 12:21:59 -0500 (Wed, 26 Feb 2014) $
 */
public class ObjectValueTranscoder implements ValueTranscoder<Object>
{


  /** {@inheritDoc} */
  @Override
  public Object decodeStringValue(final String value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public Object decodeBinaryValue(final byte[] value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final Object value)
  {
    return value.toString();
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encodeBinaryValue(final Object value)
  {
    return LdapUtils.utf8Encode(encodeStringValue(value));
  }


  /** {@inheritDoc} */
  @Override
  public Class<Object> getType()
  {
    return Object.class;
  }
}
