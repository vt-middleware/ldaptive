/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ldaptive.transcode.ValueTranscoder;

/**
 * Reflection transcoder which expects to operate on collections containing a single value.
 *
 * @param  <T>  type of object to transcode
 *
 * @author  Middleware Services
 */
public class SingleValueReflectionTranscoder<T> implements ReflectionTranscoder
{

  /** Underlying value transcoder. */
  private final ValueTranscoder<T> valueTranscoder;


  /**
   * Creates a new single value reflection transcoder.
   *
   * @param  transcoder  for a single value
   */
  public SingleValueReflectionTranscoder(final ValueTranscoder<T> transcoder)
  {
    valueTranscoder = transcoder;
  }


  /**
   * Creates a new single value reflection transcoder. Useful when the type of the value transcoder is unknown.
   *
   * @param  <T>  type to transcode
   * @param  transcoder  for a single value
   *
   * @return  single value reflection transcoder
   */
  public static <T> SingleValueReflectionTranscoder<T> newInstance(final ValueTranscoder<T> transcoder)
  {
    return new SingleValueReflectionTranscoder<>(transcoder);
  }


  @Override
  public Object decodeStringValues(final Collection<String> values)
  {
    if (values != null && values.size() > 1) {
      throw new IllegalArgumentException("Multiple values not supported");
    }
    if (values != null && !values.isEmpty()) {
      return valueTranscoder.decodeStringValue(values.iterator().next());
    }
    return null;
  }


  @Override
  public Object decodeBinaryValues(final Collection<byte[]> values)
  {
    if (values != null && values.size() > 1) {
      throw new IllegalArgumentException("Multiple values not supported");
    }
    if (values != null && !values.isEmpty()) {
      return valueTranscoder.decodeBinaryValue(values.iterator().next());
    }
    return null;
  }


  @Override
  public Collection<String> encodeStringValues(final Object value)
  {
    final List<String> l = new ArrayList<>(1);
    if (value != null) {
      @SuppressWarnings("unchecked")
      final String s = valueTranscoder.encodeStringValue((T) value);
      if (s != null) {
        l.add(s);
      }
    }
    return l;
  }


  @Override
  public Collection<byte[]> encodeBinaryValues(final Object value)
  {
    final List<byte[]> l = new ArrayList<>(1);
    if (value != null) {
      @SuppressWarnings("unchecked")
      final byte[] b = valueTranscoder.encodeBinaryValue((T) value);
      if (b != null) {
        l.add(b);
      }
    }
    return l;
  }


  @Override
  public Class<?> getType()
  {
    return valueTranscoder.getType();
  }


  @Override
  public boolean supports(final Class<?> type)
  {
    return getType().equals(type);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("valueTranscoder=").append(valueTranscoder).append("]").toString();
  }
}
