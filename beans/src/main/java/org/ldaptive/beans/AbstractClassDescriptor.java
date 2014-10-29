/*
  $Id: AbstractClassDescriptor.java 3013 2014-07-02 15:26:52Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3013 $
  Updated: $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.beans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapUtils;
import org.ldaptive.SortBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of a class descriptor. Stores a map of {@link
 * AttributeValueMutator} and a {@link DnValueMutator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3013 $ $Date: 2014-07-02 11:26:52 -0400 (Wed, 02 Jul 2014) $
 */
public abstract class AbstractClassDescriptor implements ClassDescriptor
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Attribute value mutators for this class. */
  private final Map<String, AttributeValueMutator> attributeMutators =
    new HashMap<String, AttributeValueMutator>();

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
      throw new IllegalStateException(
        "Found duplicate attribute name '" + mutator.getName() + "'");
    }
    attributeMutators.put(mutator.getName(), mutator);
  }


  /**
   * Adds attribute value mutators to this class descriptor.
   *
   * @param  mutators  to add
   */
  protected void addAttributeValueMutator(
    final Collection<AttributeValueMutator> mutators)
  {
    for (AttributeValueMutator mutator : mutators) {
      addAttributeValueMutator(mutator);
    }
  }


  /** {@inheritDoc} */
  @Override
  public DnValueMutator getDnValueMutator()
  {
    return dnMutator;
  }


  /** {@inheritDoc} */
  @Override
  public Collection<AttributeValueMutator> getAttributeValueMutators()
  {
    return attributeMutators.values();
  }


  /** {@inheritDoc} */
  @Override
  public AttributeValueMutator getAttributeValueMutator(final String name)
  {
    return attributeMutators.get(name);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::dnMutator=%s, attributeMutators=%s]",
        getClass().getName(),
        hashCode(),
        dnMutator,
        attributeMutators);
  }


  /**
   * Stores the DN value from a {@link Entry} configuration. Setter method is a
   * no-op.
   */
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


    /** {@inheritDoc} */
    @Override
    public String getValue(final Object object)
    {
      return dn;
    }


    /** {@inheritDoc} */
    @Override
    public void setValue(final Object object, final String value) {}
  }


  /**
   * Stores an {@link Attribute} configuration in an {@link LdapAttribute}
   * object. Setter methods are no-ops.
   */
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
     * @param  behavior  sort behavior of the attribute
     */
    public SimpleAttributeValueMutator(
      final String name,
      final String[] values,
      final boolean binary,
      final SortBehavior behavior)
    {
      la = new LdapAttribute(behavior, binary);
      la.setName(name);
      if (binary) {
        for (String value : values) {
          la.addBinaryValue(LdapUtils.base64Decode(value));
        }
      } else {
        la.addStringValue(values);
      }
    }


    /** {@inheritDoc} */
    @Override
    public String getName()
    {
      return la.getName();
    }


    /** {@inheritDoc} */
    @Override
    public boolean isBinary()
    {
      return la.isBinary();
    }


    /** {@inheritDoc} */
    @Override
    public SortBehavior getSortBehavior()
    {
      return la.getSortBehavior();
    }


    /** {@inheritDoc} */
    @Override
    public Collection<String> getStringValues(final Object object)
    {
      return la.getStringValues();
    }


    /** {@inheritDoc} */
    @Override
    public Collection<byte[]> getBinaryValues(final Object object)
    {
      return la.getBinaryValues();
    }


    /** {@inheritDoc} */
    @Override
    public void setStringValues(
      final Object object,
      final Collection<String> values) {}


    /** {@inheritDoc} */
    @Override
    public void setBinaryValues(
      final Object object,
      final Collection<byte[]> values) {}


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::ldapAttribute=%s]",
          getClass().getName(),
          hashCode(),
          la);
    }
  }
}
