/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import org.ldaptive.LdapUtils;

/**
 * Container for a RDN name value pair.
 *
 * @author  Middleware Services
 */
public class NameValue
{
  /** hash code seed. */
  private static final int HASH_CODE_SEED = 5011;

  /** Attribute name. */
  private final String attributeName;

  /** Attribute value. */
  private final String attributeValue;


  /**
   * Creates a new name value.
   *
   * @param  name  of the attribute
   * @param  value  of the attribute
   */
  public NameValue(final String name, final String value)
  {
    attributeName = name;
    attributeValue = value;
  }


  /**
   * Returns the attribute name.
   *
   * @return  attribute name
   */
  public String getAttributeName()
  {
    return attributeName;
  }


  /**
   * Returns the attribute value.
   *
   * @return  attribute value
   */
  public String getAttributeValue()
  {
    return attributeValue;
  }


  /**
   * Returns whether the attribute name matches the supplied name.
   *
   * @param  name  to match
   *
   * @return  whether name matches the attribute name
   */
  public boolean hasName(final String name)
  {
    return attributeName.equalsIgnoreCase(name);
  }


  /**
   * Returns a string representation of this name value, of the form 'name=value'.
   *
   * @return  string form of the name value pair
   */
  public String format()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(attributeName).append("=").append(attributeValue);
    return sb.toString();
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof NameValue) {
      final NameValue v = (NameValue) o;
      return LdapUtils.areEqual(attributeName, v.attributeName) &&
        LdapUtils.areEqual(attributeValue, v.attributeValue);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeName, attributeValue);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("name=").append(attributeName).append(", ")
      .append("value=").append(attributeValue).toString();
  }
}
