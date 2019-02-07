/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.convert;

import java.time.ZonedDateTime;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.springframework.core.convert.converter.Converter;

/**
 * Convert a String to a {@link ZonedDateTime}.
 *
 * @author  Middleware Services
 */
public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime>
{


  @Override
  public ZonedDateTime convert(final String s)
  {
    final GeneralizedTimeValueTranscoder transcoder = new GeneralizedTimeValueTranscoder();
    return transcoder.decodeStringValue(s);
  }
}
