/*
  $Id: TranscoderFactory.java 3013 2014-07-02 15:26:52Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3013 $
  Updated: $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.beans.reflect;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.io.ValueTranscoder;

/**
 * Creates value transcoders and stores them in a static map.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
 */
public final class TranscoderFactory
{

  /** Value transcoders. */
  private static final Map<String, ValueTranscoder<?>> TRANSCODERS =
    new HashMap<>();


  /** Default constructor. */
  private TranscoderFactory() {}


  /**
   * Returns a value transcoder for the supplied type. If the type cannot be
   * found it is instantiated and cached for future use.
   *
   * @param  type  of value transcoder
   *
   * @return  value transcoder
   */
  public static ValueTranscoder<?> getInstance(final String type)
  {
    if (type == null || "".equals(type)) {
      return null;
    }

    ValueTranscoder<?> transcoder;
    synchronized (TRANSCODERS) {
      if (!TRANSCODERS.containsKey(type)) {
        transcoder = createValueTranscoder(type);
        TRANSCODERS.put(type, transcoder);
      } else {
        transcoder = TRANSCODERS.get(type);
      }
    }
    return transcoder;
  }


  /**
   * Creates a value transcoder for the supplied type.
   *
   * @param  type  to create value transcoder for
   *
   * @return  value transcoder
   *
   * @throws  IllegalArgumentException  if the supplied type cannot be
   * instantiated
   */
  protected static ValueTranscoder<?> createValueTranscoder(final String type)
  {
    try {
      return (ValueTranscoder<?>) Class.forName(type).newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not instantiate transcoder", e);
    }
  }
}
