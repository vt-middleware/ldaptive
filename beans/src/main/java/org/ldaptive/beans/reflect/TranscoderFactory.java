/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.transcode.ValueTranscoder;

/**
 * Creates value transcoders and stores them in a static map.
 *
 * @author  Middleware Services
 */
public final class TranscoderFactory
{

  /** Value transcoders. */
  private static final Map<String, ValueTranscoder<?>> TRANSCODERS = new HashMap<>();


  /** Default constructor. */
  private TranscoderFactory() {}


  /**
   * Returns a value transcoder for the supplied type. If the type cannot be found it is instantiated and cached for
   * future use.
   *
   * @param  type  of value transcoder
   *
   * @return  value transcoder
   */
  public static ValueTranscoder<?> getInstance(final String type)
  {
    if (type == null || type.isEmpty()) {
      return null;
    }

    final ValueTranscoder<?> transcoder;
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
   * @throws  IllegalArgumentException  if the supplied type cannot be instantiated
   */
  private static ValueTranscoder<?> createValueTranscoder(final String type)
  {
    try {
      return (ValueTranscoder<?>) Class.forName(type).getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not instantiate transcoder", e);
    }
  }
}
