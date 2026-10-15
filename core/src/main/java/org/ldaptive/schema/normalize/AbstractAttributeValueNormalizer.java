/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base class for attribute value normalizers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAttributeValueNormalizer implements AttributeValueNormalizer
{

  /** Attribute value normalizer. */
  private final Function<String, String> valueNormalizer;


  /**
   * Creates a new abstract attribute value normalizer.
   *
   * @param  normalizer  function to normalize individual values
   */
  public AbstractAttributeValueNormalizer(final Function<String, String> normalizer)
  {
    valueNormalizer = normalizer;
  }


  @Override
  public Map<String, List<String>> normalize(final Collection<String> values)
  {
    final Map<String, List<String>> normalizedValues = new LinkedHashMap<>(values.size());
    for (String value : values) {
      final String normalized = valueNormalizer.apply(value);
      if (normalizedValues.containsKey(normalized)) {
        normalizedValues.get(normalized).add(value);
      } else {
        final List<String> l = new ArrayList<>();
        l.add(value);
        normalizedValues.put(normalized, l);
      }
    }
    return Collections.unmodifiableMap(normalizedValues);
  }
}
