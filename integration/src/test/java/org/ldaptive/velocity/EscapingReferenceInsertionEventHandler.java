/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;
import org.ldaptive.FilterTemplate;

/**
 * Insertion event handler that encodes the inserted value using {@link FilterTemplate#encodeValue(String)} or {@link
 * FilterTemplate#encodeValue(byte[])}
 *
 * @author  Middleware Services
 */
public class EscapingReferenceInsertionEventHandler implements ReferenceInsertionEventHandler
{


  @Override
  public Object referenceInsert(final Context context, final String reference, final Object value)
  {
    Object output = null;
    if (value != null) {
      if (value instanceof Object[]) {
        final List<Object> encodedValues = new ArrayList<>();
        for (Object o : (Object[]) value) {
          encodedValues.add(encode(o));
        }
        output = encodedValues.toArray();
      } else if (value instanceof Collection<?>) {
        output = ((Collection<?>) value).stream().map(this::encode).collect(Collectors.toList());
      } else {
        output = encode(value);
      }
    }
    return output;
  }


  /**
   * Returns {@link FilterTemplate#encodeValue} if value is a string.
   *
   * @param value to encode
   *
   * @return encoded value if value is a string
   */
  private Object encode(final Object value)
  {
    if (value == null) {
      return null;
    }

    final String s;
    if (value instanceof String){
      s = FilterTemplate.encodeValue((String) value);
    } else if (value instanceof byte[]) {
      s = FilterTemplate.encodeValue((byte[]) value);
    } else {
      s = FilterTemplate.encodeValue(value.toString());
    }
    return s;
  }
}
