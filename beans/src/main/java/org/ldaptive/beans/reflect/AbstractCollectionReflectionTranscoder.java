/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reflection transcoder for an object that implements a {@link Collection}.
 *
 * @author  Middleware Services
 */
public abstract class AbstractCollectionReflectionTranscoder implements ReflectionTranscoder
{

  /** Type that is a collection. */
  private final Class<?> type;

  /** Used for collections that do not contain arrays. */
  private final SingleValueReflectionTranscoder<?> singleValueTranscoder;

  /** Used for collections that contain arrays. */
  private final ArrayReflectionTranscoder arrayTranscoder;


  /**
   * Creates a new abstract collection reflection transcoder.
   *
   * @param  c  class that is a collection
   * @param  transcoder  to operate on elements of the collection
   */
  public AbstractCollectionReflectionTranscoder(final Class<?> c, final SingleValueReflectionTranscoder<?> transcoder)
  {
    type = c;
    singleValueTranscoder = transcoder;
    arrayTranscoder = null;
  }


  /**
   * Creates a new abstract collection reflection transcoder.
   *
   * @param  c  class that is a collection
   * @param  transcoder  to operate on elements of the collection
   */
  public AbstractCollectionReflectionTranscoder(final Class<?> c, final ArrayReflectionTranscoder transcoder)
  {
    type = c;
    singleValueTranscoder = null;
    arrayTranscoder = transcoder;
  }


  @Override
  public Object decodeStringValues(final Collection<String> values)
  {
    final Collection<Object> decoded = createCollection(Object.class);
    if (arrayTranscoder != null) {
      decoded.add(arrayTranscoder.decodeStringValues(values));
    } else {
      for (String value : values) {
        final List<String> l = new ArrayList<>(1);
        l.add(value);
        decoded.add(singleValueTranscoder.decodeStringValues(l));
      }
    }
    return decoded;
  }


  @Override
  public Object decodeBinaryValues(final Collection<byte[]> values)
  {
    final Collection<Object> decoded = createCollection(Object.class);
    if (arrayTranscoder != null) {
      decoded.add(arrayTranscoder.decodeBinaryValues(values));
    } else {
      for (byte[] value : values) {
        final List<byte[]> l = new ArrayList<>(1);
        l.add(value);
        decoded.add(singleValueTranscoder.decodeBinaryValues(l));
      }
    }
    return decoded;
  }


  @Override
  public Collection<String> encodeStringValues(final Object values)
  {
    final Collection<String> encoded = createCollection(String.class);
    if (values != null) {
      for (Object o : (Collection<?>) values) {
        if (arrayTranscoder != null) {
          encoded.addAll(arrayTranscoder.encodeStringValues(o));
        } else {
          encoded.addAll(singleValueTranscoder.encodeStringValues(o));
        }
      }
    }
    return encoded;
  }


  @Override
  public Collection<byte[]> encodeBinaryValues(final Object values)
  {
    final Collection<byte[]> encoded = createCollection(byte[].class);
    if (values != null) {
      for (Object o : (Collection<?>) values) {
        if (arrayTranscoder != null) {
          encoded.addAll(arrayTranscoder.encodeBinaryValues(o));
        } else {
          encoded.addAll(singleValueTranscoder.encodeBinaryValues(o));
        }
      }
    }
    return encoded;
  }


  /**
   * Returns a collection implementation of the correct type for this transcoder.
   *
   * @param  <T>  type of collection
   * @param  clazz  type of collection
   *
   * @return  collection implementation
   */
  protected abstract <T> Collection<T> createCollection(Class<T> clazz);


  @Override
  public Class<?> getType()
  {
    return type;
  }


  @Override
  public boolean supports(final Class<?> t)
  {
    return getType().isAssignableFrom(t);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "type=" + type + ", " +
      "singleValueTranscoder=" + singleValueTranscoder + ", " +
      "arrayTranscoder=" + arrayTranscoder + "]";
  }
}
