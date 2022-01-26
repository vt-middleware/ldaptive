/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import java.util.Collection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of an ldap entry mapper. Uses a {@link ClassDescriptor} for decoding and encoding of objects.
 *
 * @param  <T>  type of object to map
 *
 * @author  Middleware Services
 */
public abstract class AbstractLdapEntryMapper<T> implements LdapEntryMapper<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /**
   * Returns the class descriptor.
   *
   * @param  object  to return the class descriptor for
   *
   * @return  class descriptor
   */
  protected abstract ClassDescriptor getClassDescriptor(T object);


  @Override
  public String mapDn(final T object)
  {
    final ClassDescriptor descriptor = getClassDescriptor(object);
    final DnValueMutator dnMutator = descriptor.getDnValueMutator();
    return dnMutator.getValue(object);
  }


  /**
   * Injects data from the supplied source object into a new instance of ldap entry.
   *
   * @param  source  to read from
   *
   * @return  ldap entry
   */
  public LdapEntry map(final T source)
  {
    final LdapEntry dest = new LdapEntry();
    map(source, dest);
    return dest;
  }


  @Override
  public void map(final T source, final LdapEntry dest)
  {
    final ClassDescriptor descriptor = getClassDescriptor(source);
    final DnValueMutator dnMutator = descriptor.getDnValueMutator();
    if (dnMutator != null) {
      dest.setDn(dnMutator.getValue(source));
    }
    for (AttributeValueMutator mutator : descriptor.getAttributeValueMutators()) {
      logger.trace("using mutator {}", mutator);
      if (mutator != null) {
        final LdapAttribute attr = new LdapAttribute();
        attr.setBinary(mutator.isBinary());
        attr.setName(mutator.getName());
        if (attr.isBinary()) {
          final Collection<byte[]> c = mutator.getBinaryValues(source);
          if (c != null) {
            attr.addBinaryValues(c);
          }
        } else {
          final Collection<String> c = mutator.getStringValues(source);
          if (c != null) {
            attr.addStringValues(c);
          }
        }
        if (attr.size() > 0) {
          dest.addAttributes(attr);
        }
      }
    }
    logger.debug("Mapped {} to {}", source, dest);
  }


  @Override
  public void map(final LdapEntry source, final T dest)
  {
    final ClassDescriptor descriptor = getClassDescriptor(dest);
    final DnValueMutator dnMutator = descriptor.getDnValueMutator();
    if (dnMutator != null) {
      dnMutator.setValue(dest, source.getDn());
    }
    source.getAttributes().stream().filter(attr -> attr.size() > 0).forEach(attr -> {
      final AttributeValueMutator mutator = descriptor.getAttributeValueMutator(attr.getName());
      logger.trace("using mutator {} for attribute {}", mutator, attr);
      if (mutator != null) {
        if (attr.isBinary()) {
          mutator.setBinaryValues(dest, attr.getBinaryValues());
        } else {
          mutator.setStringValues(dest, attr.getStringValues());
        }
      }
    });
    logger.debug("Mapped {} to {}", source, dest);
  }
}
