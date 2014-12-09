/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Contains the data required to modify an ldap attribute.
 *
 * @author  Middleware Services
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
