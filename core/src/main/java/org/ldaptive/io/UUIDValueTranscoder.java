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

import java.util.UUID;

/**
 * Decodes and encodes a UUID for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2994 $ $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
 */
public class UUIDValueTranscoder extends AbstractStringValueTranscoder<UUID>
{


  /** {@inheritDoc} */
  @Override
  public UUID decodeStringValue(final String value)
  {
    return UUID.fromString(value);
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final UUID value)
  {
    return value.toString();
  }


  /** {@inheritDoc} */
  @Override
  public Class<UUID> getType()
  {
    return UUID.class;
  }
}
