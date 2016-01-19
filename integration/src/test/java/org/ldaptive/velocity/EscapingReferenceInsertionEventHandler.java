/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.velocity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.ldaptive.SearchFilter;

/**
 * Insertion event handler that encodes the inserted value using {@link SearchFilter#encodeValue(String)} or {@link
 * SearchFilter#encodeValue(byte[])}
 *
 * @author  Middleware Services
 */
public class EscapingReferenceInsertionEventHandler implements ReferenceInsertionEventHandler
{


  @Override
  public Object referenceInsert(final String reference, final Object value)
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
        final List<Object> encodedValues = new ArrayList<>();
        for (Object o : (Collection<?>) value) {
          encodedValues.add(encode(o));
        }
        output = encodedValues;
      } else {
        output = encode(value);
      }
    }
    return output;
  }


  /**
   * Returns {@link SearchFilter#encodeValue} if value is a string.
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

    String s;
    if (value instanceof String){
      s = SearchFilter.encodeValue((String) value);
    } else if (value instanceof byte[]) {
      s = SearchFilter.encodeValue((byte[]) value);
    } else {
      s = SearchFilter.encodeValue(value.toString());
    }
    return s;
  }
}
