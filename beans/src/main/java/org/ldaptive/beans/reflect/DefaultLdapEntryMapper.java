/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.reflect;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.beans.AbstractLdapEntryMapper;
import org.ldaptive.beans.ClassDescriptor;

/**
 * Stores the class descriptors for a specific object in a static map.
 *
 * @author  Middleware Services
 */
public class DefaultLdapEntryMapper extends AbstractLdapEntryMapper<Object>
{

  /** Class descriptors for mapping objects. */
  private static final Map<Class<?>, ClassDescriptor> CLASS_DESCRIPTORS = new HashMap<>();


  @Override
  protected ClassDescriptor getClassDescriptor(final Object object)
  {
    ClassDescriptor descriptor;
    final Class<?> type = object.getClass();
    synchronized (CLASS_DESCRIPTORS) {
      if (!CLASS_DESCRIPTORS.containsKey(type)) {
        descriptor = createClassDescriptor(type);
        CLASS_DESCRIPTORS.put(type, descriptor);
      } else {
        descriptor = CLASS_DESCRIPTORS.get(type);
      }
    }
    return descriptor;
  }


  /**
   * Creates a class descriptor for the supplied type.
   *
   * @param  type  to create class descriptor for
   *
   * @return  class descriptor
   */
  protected ClassDescriptor createClassDescriptor(final Class<?> type)
  {
    final DefaultClassDescriptor descriptor = new DefaultClassDescriptor();
    descriptor.initialize(type);
    return descriptor;
  }
}
