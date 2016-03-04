/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring.convert;

import java.time.Duration;
import org.springframework.core.convert.converter.Converter;

/**
 * Convert a {@link Duration} to a String.
 *
 * @author  Middleware Services
 */
public class DurationToStringConverter implements Converter<Duration, String>
{


  @Override
  public String convert(final Duration d)
  {
    return d.toString();
  }
}
