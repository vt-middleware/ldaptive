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
package org.ldaptive.ad.io;

import org.ldaptive.io.AbstractStringValueTranscoder;

/**
 * Decodes and encodes an active directory delta time value for use in an ldap
 * attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 3006 $ $Date: 2014-07-02 10:22:50 -0400 (Wed, 02 Jul 2014) $
 */
public class DeltaTimeValueTranscoder
  extends AbstractStringValueTranscoder<Long>
{

  /**
   * Delta time uses 100-nanosecond intervals. For conversion purposes this is
   * 1x10^6 / 100.
   */
  private static final long ONE_HUNDRED_NANOSECOND_INTERVAL = 10000L;


  /** {@inheritDoc} */
  @Override
  public Long decodeStringValue(final String value)
  {
    return -Long.parseLong(value) / ONE_HUNDRED_NANOSECOND_INTERVAL;
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final Long value)
  {
    return String.valueOf(-value * ONE_HUNDRED_NANOSECOND_INTERVAL);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Long> getType()
  {
    return Long.class;
  }
}
