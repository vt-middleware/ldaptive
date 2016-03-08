/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.convert;

import java.time.ZonedDateTime;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.springframework.core.convert.converter.Converter;

/**
 * Convert a {@link ZonedDateTime} to a String.
 *
 * @author  Middleware Services
 */
public class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String>
{


  @Override
  public String convert(final ZonedDateTime t)
  {
    final GeneralizedTimeValueTranscoder transcoder = new GeneralizedTimeValueTranscoder();
    return transcoder.encodeStringValue(t);
  }
}
