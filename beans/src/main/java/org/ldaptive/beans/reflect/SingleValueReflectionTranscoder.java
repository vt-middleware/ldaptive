/*
  $Id: SingleValueReflectionTranscoder.java 3013 2014-07-02 15:26:52Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3013 $
  Updated: $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.beans.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ldaptive.io.ValueTranscoder;

/**
 * Reflection transcoder which expects to operate on collections containing a
 * single value.
 *
 * @param  <T>  type of object to transcode
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
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
   * Creates a new single value reflection transcoder. Useful when the type of
   * the value transcoder is unknown.
   *
   * @param  <T>  type to transcode
   * @param  transcoder  for a single value
   *
   * @return  single value reflection transcoder
   */
  public static <T> SingleValueReflectionTranscoder<T> newInstance(
    final ValueTranscoder<T> transcoder)
  {
    return new SingleValueReflectionTranscoder<T>(transcoder);
  }


  /** {@inheritDoc} */
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


  /** {@inheritDoc} */
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


  /** {@inheritDoc} */
  @Override
  public Collection<String> encodeStringValues(final Object value)
  {
    final List<String> l = new ArrayList<String>(1);
    if (value != null) {
      @SuppressWarnings("unchecked")
      final String s = valueTranscoder.encodeStringValue((T) value);
      if (s != null) {
        l.add(s);
      }
    }
    return l;
  }


  /** {@inheritDoc} */
  @Override
  public Collection<byte[]> encodeBinaryValues(final Object value)
  {
    final List<byte[]> l = new ArrayList<byte[]>(1);
    if (value != null) {
      @SuppressWarnings("unchecked")
      final byte[] b = valueTranscoder.encodeBinaryValue((T) value);
      if (b != null) {
        l.add(b);
      }
    }
    return l;
  }


  /** {@inheritDoc} */
  @Override
  public Class<?> getType()
  {
    return valueTranscoder.getType();
  }


  /** {@inheritDoc} */
  @Override
  public boolean supports(final Class<?> type)
  {
    return getType().equals(type);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::valueTranscoder=%s]",
        getClass().getName(),
        hashCode(),
        valueTranscoder);
  }
}
