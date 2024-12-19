/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.convert;

import java.time.Duration;
import org.springframework.core.convert.converter.Converter;

/**
 * Convert a String to a {@link Duration}.
 *
 * @author  Middleware Services
 */
public class StringToDurationConverter implements Converter<String, Duration>
{


  @Override
  public Duration convert(final String s)
  {
    return Duration.parse(s);
  }
}
