/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ldaptive.SortBehavior;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.AttributeValueMutator;
import org.ldaptive.io.ValueTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * Attribute mutator that uses a SPEL expression and evaluation context.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
 */
public class SpelAttributeValueMutator implements AttributeValueMutator
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Attribute containing the SPEL expression. */
  private final Attribute attribute;

  /** SPEL expression to access values. */
  private final Expression expression;

  /** Evaluation context. */
  private final EvaluationContext evaluationContext;

  /** Custom transcoder for this attribute. */
  private final ValueTranscoder transcoder;


  /**
   * Creates a new spel attribute value mutator.
   *
   * @param  attr  containing the SPEL configuration
   * @param  context  containing the values
   */
  public SpelAttributeValueMutator(
    final Attribute attr,
    final EvaluationContext context)
  {
    attribute = attr;

    final ExpressionParser parser = new SpelExpressionParser();
    expression = parser.parseExpression(
      attribute.property().length() > 0 ?
        attribute.property() : attribute.name());
    evaluationContext = context;
    if ("".equals(attribute.transcoder())) {
      transcoder = null;
    } else {
      transcoder = parser.parseExpression(attribute.transcoder()).getValue(
        ValueTranscoder.class);
    }
  }


  /** {@inheritDoc} */
  @Override
  public String getName()
  {
    return attribute.name();
  }


  /** {@inheritDoc} */
  @Override
  public boolean isBinary()
  {
    return attribute.binary();
  }


  /** {@inheritDoc} */
  @Override
  public SortBehavior getSortBehavior()
  {
    return attribute.sortBehavior();
  }


  /** {@inheritDoc} */
  @Override
  public Collection<String> getStringValues(final Object object)
  {
    return getValues(object, String.class);
  }


  /** {@inheritDoc} */
  @Override
  public Collection<byte[]> getBinaryValues(final Object object)
  {
    return getValues(object, byte[].class);
  }


  /**
   * Uses the configured expression and evaluation context to retrieve values
   * from the supplied object. Values are the placed in a collection and
   * returned.
   *
   * @param  <T>  either String or byte[]
   * @param  object  to get values from
   * @param  type  of objects to place in the collection
   *
   * @return  values in the supplied object
   */
  protected <T> Collection<T> getValues(
    final Object object,
    final Class<T> type)
  {
    Collection<T> values = null;
    final Object converted = expression.getValue(evaluationContext, object);
    if (converted != null) {
      if (converted.getClass().isArray()) {
        final int length = Array.getLength(converted);
        values = createCollection(List.class, length);
        for (int i = 0; i < length; i++) {
          final Object o = Array.get(converted, i);
          if (o != null) {
            final T value = convertValue(o, o.getClass(), type);
            if (value != null) {
              values.add(value);
            }
          }
        }
      } else if (Collection.class.isAssignableFrom(converted.getClass())) {
        final Collection<?> col = (Collection<?>) converted;
        values = createCollection(converted.getClass(), col.size());
        for (Object o : col) {
          if (o != null) {
            final T value = convertValue(o, o.getClass(), type);
            if (value != null) {
              values.add(value);
            }
          }
        }
      } else {
        values = createCollection(List.class, 1);

        final T value = convertValue(converted, converted.getClass(), type);
        if (value != null) {
          values.add(value);
        }
      }
    }
    return values;
  }


  /**
   * Converts the supplied value to the target type. If a custom transcoder has
   * been configured it is used. Otherwise the type converter from the
   * evaluation context is used.
   *
   * @param  <T>  either String or byte[]
   * @param  value  to convert
   * @param  sourceType  to convert from
   * @param  targetType  to convert to
   *
   * @return  converted value
   */
  @SuppressWarnings("unchecked")
  protected <T> T convertValue(
    final Object value,
    final Class<?> sourceType,
    final Class<T> targetType)
  {
    T converted;
    if (transcoder != null) {
      if (byte[].class == targetType) {
        converted = (T) transcoder.encodeBinaryValue(value);
      } else if (String.class == targetType) {
        converted = (T) transcoder.encodeStringValue(value);
      } else {
        throw new IllegalArgumentException(
          "targetType must be either String.class or byte[].class");
      }
    } else {
      converted = (T) evaluationContext.getTypeConverter().convertValue(
        value,
        TypeDescriptor.valueOf(sourceType),
        TypeDescriptor.valueOf(targetType));
    }
    return converted;
  }


  /** {@inheritDoc} */
  @Override
  public void setStringValues(
    final Object object,
    final Collection<String> values)
  {
    setValues(object, values, String.class);
  }


  /** {@inheritDoc} */
  @Override
  public void setBinaryValues(
    final Object object,
    final Collection<byte[]> values)
  {
    setValues(object, values, byte[].class);
  }


  /**
   * Uses the configured expression and evaluation context to set values on the
   * supplied object. If a custom transcoder has been configured it is executed
   * on the values before they are passed to the expression.
   *
   * @param  <T>  either String or byte[]
   * @param  object  to set values on
   * @param  values  to set
   * @param  type  of objects in the collection
   */
  protected <T> void setValues(
    final Object object,
    final Collection<T> values,
    final Class<T> type)
  {
    if (transcoder != null) {
      final Collection<Object> newValues = createCollection(
        values.getClass(),
        values.size());
      for (T t : values) {
        if (byte[].class == type) {
          newValues.add(transcoder.decodeBinaryValue((byte[]) t));
        } else if (String.class == type) {
          newValues.add(transcoder.decodeStringValue((String) t));
        } else {
          throw new IllegalArgumentException(
            "type must be either String.class or byte[].class");
        }
      }
      expression.setValue(evaluationContext, object, newValues);
    } else {
      expression.setValue(evaluationContext, object, values);
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::attribute=%s, expression=%s, evaluationContext=%s, " +
        "transcoder=%s]",
        getClass().getName(),
        hashCode(),
        attribute,
        expression,
        evaluationContext,
        transcoder);
  }


  /**
   * Creates a best fit collection for the supplied type.
   *
   * @param  <T>  collection type
   * @param  type  of collection to create
   * @param  size  of the collection
   *
   * @return  collection
   */
  protected static <T> Collection<T> createCollection(
    final Class<?> type,
    final int size)
  {
    Collection<T> c;
    if (List.class.isAssignableFrom(type)) {
      if (LinkedList.class.isAssignableFrom(type)) {
        c = new LinkedList<>();
      } else {
        c = new ArrayList<>(size);
      }
    } else if (Set.class.isAssignableFrom(type)) {
      if (LinkedHashSet.class.isAssignableFrom(type)) {
        c = new LinkedHashSet<>(size);
      } else if (TreeSet.class.isAssignableFrom(type)) {
        c = new TreeSet<>();
      } else {
        c = new HashSet<>(size);
      }
    } else {
      c = new ArrayList<>(size);
    }
    return c;
  }
}
