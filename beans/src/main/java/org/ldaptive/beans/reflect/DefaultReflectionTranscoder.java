/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ldaptive.io.BooleanValueTranscoder;
import org.ldaptive.io.ByteArrayValueTranscoder;
import org.ldaptive.io.CertificateValueTranscoder;
import org.ldaptive.io.CharArrayValueTranscoder;
import org.ldaptive.io.DoubleValueTranscoder;
import org.ldaptive.io.FloatValueTranscoder;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.ldaptive.io.IntegerValueTranscoder;
import org.ldaptive.io.LongValueTranscoder;
import org.ldaptive.io.ObjectValueTranscoder;
import org.ldaptive.io.ShortValueTranscoder;
import org.ldaptive.io.StringValueTranscoder;
import org.ldaptive.io.UUIDValueTranscoder;
import org.ldaptive.io.ValueTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a reflection transcoder. Determines the correct
 * underlying reflection transcoder by inspecting the class type
 * characteristics.
 *
 * @author  Middleware Services
 */
public class DefaultReflectionTranscoder implements ReflectionTranscoder
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Custom transcoder to override the default transcoder. */
  private final SingleValueReflectionTranscoder<?> customTranscoder;

  /** Transcoder for this type. */
  private final ReflectionTranscoder valueTranscoder;

  /** Set of transcoders support single values. */
  private final Set<SingleValueReflectionTranscoder<?>> singleValueTranscoders;


  /**
   * Creates a new default reflection transcoder.
   *
   * @param  type  of object to transcode
   */
  public DefaultReflectionTranscoder(final Type type)
  {
    this(type, null);
  }


  /**
   * Creates a new default reflection transcoder.
   *
   * @param  type  of object to transcode
   * @param  transcoder  custom transcoder for this type
   */
  public DefaultReflectionTranscoder(
    final Type type,
    final ValueTranscoder<?> transcoder)
  {
    if (transcoder != null) {
      customTranscoder = SingleValueReflectionTranscoder.newInstance(
        transcoder);
    } else {
      customTranscoder = null;
    }
    singleValueTranscoders = getDefaultSingleValueTranscoders();
    if (type instanceof Class) {
      final Class<?> c = (Class<?>) type;
      if (c.isArray()) {
        if (byte[].class == c || char[].class == c) {
          valueTranscoder = getSingleValueReflectionTranscoder(c);
        } else {
          valueTranscoder = new ArrayReflectionTranscoder(
            getSingleValueReflectionTranscoder(c.getComponentType()));
        }
      } else if (Collection.class.isAssignableFrom(c)) {
        valueTranscoder = getCollectionEncoder(c, Object.class);
      } else {
        valueTranscoder = getSingleValueReflectionTranscoder(c);
      }
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType pt = (ParameterizedType) type;
      final Type rawType = pt.getRawType();
      final Type[] typeArgs = pt.getActualTypeArguments();
      if (typeArgs.length != 1) {
        throw new IllegalArgumentException(
          "Unsupported type arguments: " + Arrays.toString(typeArgs));
      }

      final Class<?> rawClass = ReflectionUtils.classFromType(rawType);
      if (typeArgs[0] instanceof GenericArrayType) {
        final GenericArrayType gat = (GenericArrayType) typeArgs[0];
        if (Collection.class.isAssignableFrom(rawClass)) {
          valueTranscoder = getCollectionEncoder(rawClass, gat);
        } else {
          throw new IllegalArgumentException("Unsupported type: " + rawClass);
        }
      } else if (typeArgs[0] instanceof Class) {
        if (Collection.class.isAssignableFrom(rawClass)) {
          valueTranscoder = getCollectionEncoder(rawClass, typeArgs[0]);
        } else {
          throw new IllegalArgumentException("Unsupported type: " + rawClass);
        }
      } else {
        throw new IllegalArgumentException("Unsupported type: " + rawClass);
      }
    } else {
      throw new IllegalArgumentException("Unsupported type: " + type);
    }
  }


  /**
   * Initializes the set of default single value transcoders.
   *
   * @return  single value transcoders
   */
  protected Set<SingleValueReflectionTranscoder<?>>
  getDefaultSingleValueTranscoders()
  {
    final Set<SingleValueReflectionTranscoder<?>> transcoders = new HashSet<>();
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new ObjectValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new BooleanValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new BooleanValueTranscoder(true)));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new DoubleValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new DoubleValueTranscoder(true)));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new FloatValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new FloatValueTranscoder(true)));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new IntegerValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new IntegerValueTranscoder(true)));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new LongValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new LongValueTranscoder(true)));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new ShortValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new ShortValueTranscoder(true)));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new StringValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new ByteArrayValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new CharArrayValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new CertificateValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(
        new GeneralizedTimeValueTranscoder()));
    transcoders.add(
      new SingleValueReflectionTranscoder<>(new UUIDValueTranscoder()));
    return transcoders;
  }


  /**
   * Returns the appropriate single value encoder for the supplied type.
   *
   * @param  type  to provide a single value encoder for
   *
   * @return  single value reflection transcoder
   */
  protected SingleValueReflectionTranscoder getSingleValueReflectionTranscoder(
    final Class<?> type)
  {
    if (customTranscoder != null) {
      return customTranscoder;
    }
    for (SingleValueReflectionTranscoder transcoder : singleValueTranscoders) {
      @SuppressWarnings("unchecked")
      final boolean supports = transcoder.supports(type);
      if (supports) {
        return transcoder;
      }
    }
    throw new IllegalArgumentException("Unsupported type: " + type);
  }


  /**
   * Returns the appropriate collection encoder for the supplied type.
   *
   * @param  type  to provide a collection encoder for
   * @param  genericType  of the collection
   *
   * @return  reflection transcoder for a collection
   */
  protected ReflectionTranscoder getCollectionEncoder(
    final Class<?> type,
    final Type genericType)
  {
    Class<?> genericClass;
    boolean isGenericArray = false;
    if (genericType instanceof GenericArrayType) {
      final Class<?> c = ReflectionUtils.classFromType(
        ((GenericArrayType) genericType).getGenericComponentType());
      if (Byte.TYPE == c) {
        genericClass = byte[].class;
      } else if (Character.TYPE == c) {
        genericClass = char[].class;
      } else {
        genericClass = c;
        isGenericArray = true;
      }
    } else {
      genericClass = ReflectionUtils.classFromType(genericType);
    }

    ReflectionTranscoder encoder;
    if (type == Collection.class || List.class.isAssignableFrom(type)) {
      if (isGenericArray) {
        encoder = new ListReflectionTranscoder(
          type,
          new ArrayReflectionTranscoder(
            getSingleValueReflectionTranscoder(genericClass)));
      } else {
        encoder = new ListReflectionTranscoder(
          type,
          getSingleValueReflectionTranscoder(genericClass));
      }
    } else if (Set.class.isAssignableFrom(type)) {
      if (isGenericArray) {
        encoder = new SetReflectionTranscoder(
          type,
          new ArrayReflectionTranscoder(
            getSingleValueReflectionTranscoder(genericClass)));
      } else {
        encoder = new SetReflectionTranscoder(
          type,
          getSingleValueReflectionTranscoder(genericClass));
      }
    } else {
      throw new IllegalArgumentException(
        "Unsupported type: " + type + " with generic type: " + genericType);
    }
    return encoder;
  }


  @Override
  public Object decodeStringValues(final Collection<String> values)
  {
    return valueTranscoder.decodeStringValues(values);
  }


  @Override
  public Object decodeBinaryValues(final Collection<byte[]> values)
  {
    return valueTranscoder.decodeBinaryValues(values);
  }


  @Override
  public Collection<String> encodeStringValues(final Object values)
  {
    return valueTranscoder.encodeStringValues(values);
  }


  @Override
  public Collection<byte[]> encodeBinaryValues(final Object values)
  {
    return valueTranscoder.encodeBinaryValues(values);
  }


  @Override
  public Class<?> getType()
  {
    return valueTranscoder.getType();
  }


  @Override
  public boolean supports(final Class<?> type)
  {
    return valueTranscoder.supports(type);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::customTranscoder=%s, valueTranscoder=%s]",
        getClass().getName(),
        hashCode(),
        customTranscoder,
        valueTranscoder);
  }
}
