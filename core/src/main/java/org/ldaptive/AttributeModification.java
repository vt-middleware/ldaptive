/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive;

/**
 * Contains the data required to modify an ldap attribute.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AttributeModification
{

  /** Type of modification to perform. */
  private AttributeModificationType attrMod;

  /** Attribute to modify. */
  private LdapAttribute attribute;

  /** Default constructor. */
  public AttributeModification() {}


  /**
   * Creates a new attribute modification.
   *
   * @param  type  attribute modification type
   * @param  attr  to modify
   */
  public AttributeModification(
    final AttributeModificationType type,
    final LdapAttribute attr)
  {
    setAttributeModificationType(type);
    setAttribute(attr);
  }


  /**
   * Returns the attribute modification type.
   *
   * @return  attribute modification type
   */
  public AttributeModificationType getAttributeModificationType()
  {
    return attrMod;
  }


  /**
   * Sets the attribute modification type.
   *
   * @param  type  attribute modification type
   */
  public void setAttributeModificationType(final AttributeModificationType type)
  {
    attrMod = type;
  }


  /**
   * Returns the ldap attribute.
   *
   * @return  ldap attribute
   */
  public LdapAttribute getAttribute()
  {
    return attribute;
  }


  /**
   * Sets the ldap attribute.
   *
   * @param  attr  ldap attribute
   */
  public void setAttribute(final LdapAttribute attr)
  {
    attribute = attr;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::attrMod=%s, attribute=%s]",
        getClass().getName(),
        hashCode(),
        attrMod,
        attribute);
  }
}
