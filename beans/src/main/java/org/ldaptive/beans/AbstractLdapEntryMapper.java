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
    logger.trace("using dn mutator {} on {}", dnMutator, source);
    if (dnMutator != null) {
      dest.setDn(mapDn(source, dnMutator));
    }
    for (AttributeValueMutator mutator : descriptor.getAttributeValueMutators()) {
      logger.trace("using attribute value mutator {} on {}", mutator, source);
      if (mutator != null) {
        final LdapAttribute attr = mapValue(source, mutator);
        if (attr.size() > 0) {
          dest.addAttributes(attr);
        }
      }
    }
    logger.debug("Mapped {} to {}", source, dest);
  }


  /**
   * Creates a new DN using the supplied mutator and source.
   *
   * @param  source  to get DN from
   * @param  mutator  to invoke
   *
   * @return  mapped DN
   */
  protected String mapDn(final T source, final DnValueMutator mutator)
  {
    return mutator.getValue(source);
  }


  /**
   * Creates a new ldap attribute using the supplied mutator and source.
   *
   * @param  source  to get attribute values from
   * @param  mutator  to invoke
   *
   * @return  new ldap attribute containing zero or more values
   */
  protected LdapAttribute mapValue(final T source, final AttributeValueMutator mutator)
  {
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
    return attr;
  }


  @Override
  public void map(final LdapEntry source, final T dest)
  {
    final ClassDescriptor descriptor = getClassDescriptor(dest);
    final DnValueMutator dnMutator = descriptor.getDnValueMutator();
    logger.trace("using dn mutator {} for entry {}", dnMutator, source);
    if (dnMutator != null) {
      mapDn(source.getDn(), dest, dnMutator);
    }
    for (LdapAttribute attr : source.getAttributes()) {
      if (attr.size() > 0) {
        final AttributeValueMutator mutator = descriptor.getAttributeValueMutator(attr.getName());
        logger.trace("using mutator {} for attribute {}", mutator, attr);
        if (mutator != null) {
          mapValue(attr, dest, mutator);
        }
      }
    }
    logger.debug("Mapped {} to {}", source, dest);
  }


  /**
   * Sets the supplied DN on the destination using the mutator.
   *
   * @param  dn  value to set
   * @param  dest  to set value on
   * @param  mutator  to invoke
   */
  protected void mapDn(final String dn, final T dest, final DnValueMutator mutator)
  {
    mutator.setValue(dest, dn);
  }


  /**
   * Sets the supplied attribute values on the destination using the mutator.
   *
   * @param  attr  values to set
   * @param  dest  to set values on
   * @param  mutator  to invoke
   */
  protected void mapValue(final LdapAttribute attr, final T dest, final AttributeValueMutator mutator)
  {
    if (attr.isBinary()) {
      mutator.setBinaryValues(dest, attr.getBinaryValues());
    } else {
      mutator.setStringValues(dest, attr.getStringValues());
    }
  }
}
