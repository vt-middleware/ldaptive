/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import org.ldaptive.beans.AbstractClassDescriptor;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;

/**
 * Default implementation of a class descriptor. Reads the {@link Entry} annotation and sets the appropriate DN and
 * attribute value mutators.
 *
 * @author  Middleware Services
 */
public class DefaultClassDescriptor extends AbstractClassDescriptor
{


  @Override
  public void initialize(final Class<?> type)
  {
    // check for entry annotation
    final Entry entryAnnotation = type.getAnnotation(Entry.class);
    if (entryAnnotation != null) {

      // add any method descriptors that match attributes
      final MethodClassDescriptor methodDescriptor = new MethodClassDescriptor();
      methodDescriptor.initialize(type);
      if (methodDescriptor.getDnValueMutator() != null) {
        setDnValueMutator(methodDescriptor.getDnValueMutator());
      }
      addAttributeValueMutator(methodDescriptor.getAttributeValueMutators());

      // add any field descriptors that aren't available as method mutators
      final FieldClassDescriptor fieldDescriptor = new FieldClassDescriptor();
      fieldDescriptor.initialize(type);
      if (getDnValueMutator() == null && fieldDescriptor.getDnValueMutator() != null) {
        setDnValueMutator(fieldDescriptor.getDnValueMutator());
      }
      fieldDescriptor.getAttributeValueMutators().stream().filter(
        mutator -> getAttributeValueMutator(mutator.getName()) == null).forEach(this::addAttributeValueMutator);

      // add any hard coded attributes that have a values declaration
      for (final Attribute attr : entryAnnotation.attributes()) {
        if ("".equals(attr.property()) && attr.values().length > 0) {
          addAttributeValueMutator(
            new SimpleAttributeValueMutator(attr.name(), attr.values(), attr.binary(), attr.sortBehavior()));
        }
      }

      // if no DN mutator has been set, use the value in the annotation
      if (getDnValueMutator() == null) {
        setDnValueMutator(new SimpleDnValueMutator(entryAnnotation.dn()));
      }
    }
  }
}
