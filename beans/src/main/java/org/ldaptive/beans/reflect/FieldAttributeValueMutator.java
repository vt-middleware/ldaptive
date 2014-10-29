/*
  $Id: FieldAttributeValueMutator.java 3013 2014-07-02 15:26:52Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3013 $
  Updated: $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.beans.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import org.ldaptive.SortBehavior;

/**
 * Attribute mutator associated with the {@link Field} of an object.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
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
  public FieldAttributeValueMutator(
    final ReflectionTranscoder transcoder,
    final Field field)
  {
    super(null, false, null, transcoder);
    f = field;
    f.setAccessible(true);
    isFinal = Modifier.isFinal(f.getModifiers());
  }


  /**
   * Creates a new field attribute value mutator.
   *
   * @param  name  of the attribute
   * @param  binary  whether the attribute is binary
   * @param  sortBehavior  sort behavior of the attribute
   * @param  transcoder  to mutate the field
   * @param  field  to mutate
   */
  public FieldAttributeValueMutator(
    final String name,
    final boolean binary,
    final SortBehavior sortBehavior,
    final ReflectionTranscoder transcoder,
    final Field field)
  {
    super(name, binary, sortBehavior, transcoder);
    f = field;
    f.setAccessible(true);
    isFinal = Modifier.isFinal(f.getModifiers());
  }


  /** {@inheritDoc} */
  @Override
  public Collection<String> getStringValues(final Object object)
  {
    return
      getReflectionTranscoder().encodeStringValues(
        ReflectionUtils.getField(f, object));
  }


  /** {@inheritDoc} */
  @Override
  public Collection<byte[]> getBinaryValues(final Object object)
  {
    return
      getReflectionTranscoder().encodeBinaryValues(
        ReflectionUtils.getField(f, object));
  }


  /** {@inheritDoc} */
  @Override
  public void setStringValues(
    final Object object,
    final Collection<String> values)
  {
    if (!isFinal) {
      ReflectionUtils.setField(
        f,
        object,
        getReflectionTranscoder().decodeStringValues(values));
    }
  }


  /** {@inheritDoc} */
  @Override
  public void setBinaryValues(
    final Object object,
    final Collection<byte[]> values)
  {
    if (!isFinal) {
      ReflectionUtils.setField(
        f,
        object,
        getReflectionTranscoder().decodeBinaryValues(values));
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::name=%s, binary=%s, sortBehavior=%s, " +
        "reflectionTranscoder=%s, field=%s]",
        getClass().getName(),
        hashCode(),
        getName(),
        isBinary(),
        getSortBehavior(),
        getReflectionTranscoder(),
        f);
  }
}
