/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;

/**
 * Base class for attribute value assertion filters.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAttributeValueAssertionFilter implements Filter
{

  /** Type of filter. */
  protected final Filter.Type filterType;

  /** Attribute description. */
  protected final String attributeDesc;

  /** Attribute value. */
  protected final byte[] assertionValue;


  /**
   * Creates a new abstract attribute value assertion filter.
   *
   * @param  type  of filter
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public AbstractAttributeValueAssertionFilter(final Filter.Type type, final String name, final byte[] value)
  {
    filterType = type;
    attributeDesc = name;
    assertionValue = value;
  }


  /**
   * Returns the attribute description.
   *
   * @return  attribute description
   */
  public String getAttributeDesc()
  {
    return attributeDesc;
  }


  /**
   * Returns the assertion value.
   *
   * @return  assertion value
   */
  public byte[] getAssertionValue()
  {
    return assertionValue;
  }


  @Override
  public DEREncoder getEncoder()
  {
    return new ConstructedDEREncoder(
      new ContextDERTag(filterType.ordinal(), true),
      new OctetStringType(attributeDesc),
      new OctetStringType(assertionValue));
  }


  // CheckStyle:EqualsHashCode OFF
  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AbstractAttributeValueAssertionFilter) {
      final AbstractAttributeValueAssertionFilter v = (AbstractAttributeValueAssertionFilter) o;
      return LdapUtils.areEqual(filterType, v.filterType) &&
        LdapUtils.areEqual(attributeDesc, v.attributeDesc) &&
        LdapUtils.areEqual(assertionValue, v.assertionValue);
    }
    return false;
  }
  // CheckStyle:EqualsHashCode ON


  @Override
  public abstract int hashCode();


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("filterType=").append(filterType).append(", ")
      .append("attributeDesc=").append(attributeDesc).append(", ")
      .append("assertionValue=").append(
        assertionValue == null ? null : new String(assertionValue, StandardCharsets.UTF_8)).toString();
  }
}
