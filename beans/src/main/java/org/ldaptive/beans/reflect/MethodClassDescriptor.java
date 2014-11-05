/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.beans.AbstractClassDescriptor;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.AttributeValueMutator;
import org.ldaptive.beans.DnValueMutator;
import org.ldaptive.beans.Entry;
import org.ldaptive.io.ValueTranscoder;

/**
 * Creates DN and attribute mutators for the {@link java.lang.reflect.Method}s
 * on a type. Leverages the {@link Introspector} class for reading descriptors.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
 */
public class MethodClassDescriptor extends AbstractClassDescriptor
{


  /** {@inheritDoc} */
  @Override
  public void initialize(final Class<?> type)
  {
    final Map<String, PropertyDescriptor> descriptors = getPropertyDescriptors(
      type);
    final Entry entryAnnotation = type.getAnnotation(Entry.class);
    if (descriptors.containsKey(entryAnnotation.dn())) {
      setDnValueMutator(
        createDnValueMutator(descriptors.get(entryAnnotation.dn())));
    }
    for (Attribute attr : entryAnnotation.attributes()) {
      if (attr.values().length == 0) {
        final String property = attr.property().length() > 0 ?
          attr.property() : attr.name();
        if (descriptors.containsKey(property)) {
          addAttributeValueMutator(
            createAttributeValueMutator(descriptors.get(property), attr));
        }
      }
    }
  }


  /**
   * Returns a map of property descriptor names to property descriptor.
   *
   * @param  type  of class to inspect
   *
   * @return  map of name to property descriptor
   */
  protected Map<String, PropertyDescriptor> getPropertyDescriptors(
    final Class<?> type)
  {
    final Map<String, PropertyDescriptor> descriptors = new HashMap<>();
    try {
      final BeanInfo info = Introspector.getBeanInfo(type);
      if (info != null) {
        final PropertyDescriptor[] desc = info.getPropertyDescriptors();
        if (desc != null) {
          for (PropertyDescriptor pd : desc) {
            descriptors.put(pd.getName(), pd);
          }
        }
      }
    } catch (IntrospectionException e) {
      throw new IllegalArgumentException(e);
    }
    return descriptors;
  }


  /**
   * Returns a dn value mutator for the supplied property descriptor.
   *
   * @param  desc  to create dn value mutator for
   *
   * @return  dn value mutator
   */
  protected DnValueMutator createDnValueMutator(final PropertyDescriptor desc)
  {
    return
      new DefaultDnValueMutator(
        new MethodAttributeValueMutator(
          new DefaultReflectionTranscoder(
            desc.getReadMethod().getGenericReturnType()),
          desc.getReadMethod(),
          desc.getWriteMethod()));
  }


  /**
   * Returns an attribute value mutator for the supplied property descriptor.
   *
   * @param  desc  to create attribute value mutator for
   * @param  attribute  attribute containing metadata
   *
   * @return  attribute value mutator
   */
  protected AttributeValueMutator createAttributeValueMutator(
    final PropertyDescriptor desc,
    final Attribute attribute)
  {
    final String name = "".equals(attribute.name()) ?
      desc.getName() : attribute.name();
    final ValueTranscoder<?> transcoder = TranscoderFactory.getInstance(
      attribute.transcoder());
    return
      new MethodAttributeValueMutator(
        name,
        attribute.binary(),
        attribute.sortBehavior(),
        new DefaultReflectionTranscoder(
          desc.getReadMethod().getGenericReturnType(),
          transcoder),
        desc.getReadMethod(),
        desc.getWriteMethod());
  }
}
