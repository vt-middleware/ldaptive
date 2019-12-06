/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * Attribute mutator associated with the {@link Field} of an object.
 *
 * @author  Middleware Services
 */
public class FieldAttributeValueMutator extends AbstractAttributeValueMutator
{

  /** Field to operate on. */
  private final Field f;

  /** Whether the field has a final modifier. */
  private final boolean isFinal;


  /**
   * Creates a new field attribute value mutator.
   *
   * @param  transcoder  for mutating the field
   * @param  field  to mutate
   */
  public FieldAttributeValueMutator(final ReflectionTranscoder transcoder, final Field field)
  {
    super(null, false, transcoder);
    f = field;
    f.setAccessible(true);
    isFinal = Modifier.isFinal(f.getModifiers());
  }


  /**
   * Creates a new field attribute value mutator.
   *
   * @param  name  of the attribute
   * @param  binary  whether the attribute is binary
   * @param  transcoder  to mutate the field
   * @param  field  to mutate
   */
  public FieldAttributeValueMutator(
    final String name,
    final boolean binary,
    final ReflectionTranscoder transcoder,
    final Field field)
  {
    super(name, binary, transcoder);
    f = field;
    f.setAccessible(true);
    isFinal = Modifier.isFinal(f.getModifiers());
  }


  @Override
  public Collection<String> getStringValues(final Object object)
  {
    return getReflectionTranscoder().encodeStringValues(ReflectionUtils.getField(f, object));
  }


  @Override
  public Collection<byte[]> getBinaryValues(final Object object)
  {
    return getReflectionTranscoder().encodeBinaryValues(ReflectionUtils.getField(f, object));
  }


  @Override
  public void setStringValues(final Object object, final Collection<String> values)
  {
    if (!isFinal) {
      ReflectionUtils.setField(f, object, getReflectionTranscoder().decodeStringValues(values));
    }
  }


  @Override
  public void setBinaryValues(final Object object, final Collection<byte[]> values)
  {
    if (!isFinal) {
      ReflectionUtils.setField(f, object, getReflectionTranscoder().decodeBinaryValues(values));
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("name=").append(getName()).append(", ")
      .append("binary=").append(isBinary()).append(", ")
      .append("reflectionTranscoder=").append(getReflectionTranscoder()).append(", ")
      .append("field=").append(f).append("]").toString();
  }
}
