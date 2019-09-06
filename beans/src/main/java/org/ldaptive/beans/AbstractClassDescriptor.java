/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of a class descriptor. Stores a map of {@link AttributeValueMutator} and a {@link
 * DnValueMutator}.
 *
 * @author  Middleware Services
 */
public abstract class AbstractClassDescriptor implements ClassDescriptor
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Attribute value mutators for this class. */
  private final Map<String, AttributeValueMutator> attributeMutators = new LinkedHashMap<>();

  /** Dn value mutator for this class. */
  private DnValueMutator dnMutator;


  /**
   * Sets the dn value mutator.
   *
   * @param  mutator  to set
   */
  protected void setDnValueMutator(final DnValueMutator mutator)
  {
    if (dnMutator != null) {
      throw new IllegalStateException("Found duplicate dn");
    }
    dnMutator = mutator;
  }


  /**
   * Adds an attribute value mutator to this class descriptor.
   *
   * @param  mutator  to add
   */
  protected void addAttributeValueMutator(final AttributeValueMutator mutator)
  {
    if (attributeMutators.containsKey(mutator.getName())) {
      throw new IllegalStateException("Found duplicate attribute name '" + mutator.getName() + "'");
    }
    attributeMutators.put(mutator.getName(), mutator);
  }


  /**
   * Adds attribute value mutators to this class descriptor.
   *
   * @param  mutators  to add
   */
  protected void addAttributeValueMutator(final Collection<AttributeValueMutator> mutators)
  {
    mutators.forEach(this::addAttributeValueMutator);
  }


  @Override
  public DnValueMutator getDnValueMutator()
  {
    return dnMutator;
  }


  @Override
  public Collection<AttributeValueMutator> getAttributeValueMutators()
  {
    return attributeMutators.values();
  }


  @Override
  public AttributeValueMutator getAttributeValueMutator(final String name)
  {
    return attributeMutators.get(name);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("dnMutator=").append(dnMutator).append(", ")
      .append("attributeMutators=").append(attributeMutators).append("]").toString();
  }


  /** Stores the DN value from a {@link Entry} configuration. Setter method is a no-op. */
  protected class SimpleDnValueMutator implements DnValueMutator
  {

    /** DN value to store. */
    private final String dn;


    /**
     * Creates a new simple dn value mutator.
     *
     * @param  value  that is the DN
     */
    public SimpleDnValueMutator(final String value)
    {
      dn = value;
    }


    @Override
    public String getValue(final Object object)
    {
      return dn;
    }


    @Override
    public void setValue(final Object object, final String value) {}
  }


  /** Stores an {@link Attribute} configuration in an {@link LdapAttribute} object. Setter methods are no-ops. */
  protected class SimpleAttributeValueMutator implements AttributeValueMutator
  {

    /** Ldap attribute to operate on. */
    private final LdapAttribute la;


    /**
     * Creates a new simple attribute value mutator.
     *
     * @param  name  of the attribute
     * @param  values  of the attribute
     * @param  binary  whether values contains base64 encoded data
     */
    public SimpleAttributeValueMutator(final String name, final String[] values, final boolean binary)
    {
      la = new LdapAttribute();
      la.setBinary(binary);
      la.setName(name);
      if (binary) {
        for (String value : values) {
          la.addBinaryValues(LdapUtils.base64Decode(value));
        }
      } else {
        la.addStringValues(values);
      }
    }


    @Override
    public String getName()
    {
      return la.getName();
    }


    @Override
    public boolean isBinary()
    {
      return la.isBinary();
    }


    @Override
    public Collection<String> getStringValues(final Object object)
    {
      return la.getStringValues();
    }


    @Override
    public Collection<byte[]> getBinaryValues(final Object object)
    {
      return la.getBinaryValues();
    }


    @Override
    public void setStringValues(final Object object, final Collection<String> values) {}


    @Override
    public void setBinaryValues(final Object object, final Collection<byte[]> values) {}


    @Override
    public String toString()
    {
      return new StringBuilder("[").append(
        getClass().getName()).append("@").append(hashCode()).append("::")
        .append("ldapAttribute=").append(la).append("]").toString();
    }
  }
}
