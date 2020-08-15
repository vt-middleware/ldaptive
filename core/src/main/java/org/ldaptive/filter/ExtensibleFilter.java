/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;

/**
 * Extensible search filter component.
 *
 * @author  Middleware Services
 */
public class ExtensibleFilter implements Filter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10061;

  /** Matching rule id. */
  private final String matchingRuleID;

  /** Attribute description. */
  private final String attributeDesc;

  /** Attribute value. */
  private final byte[] assertionValue;

  /** DN attributes. */
  private final boolean dnAttributes;


  /**
   * Creates a new extensible filter.
   *
   * @param  matchingRule  matching rule
   * @param  type  attribute description
   * @param  value  attribute value
   */
  public ExtensibleFilter(final String matchingRule, final String type, final String value)
  {
    this(matchingRule, type, value.getBytes(StandardCharsets.UTF_8), false);
  }


  /**
   * Creates a new extensible filter.
   *
   * @param  matchingRule  matching rule
   * @param  type  attribute description
   * @param  value  attribute value
   * @param  dnAttrs  DN attributes
   */
  public ExtensibleFilter(final String matchingRule, final String type, final String value, final boolean dnAttrs)
  {
    this(matchingRule, type, value.getBytes(StandardCharsets.UTF_8), dnAttrs);
  }


  /**
   * Creates a new extensible filter.
   *
   * @param  matchingRule  matching rule
   * @param  type  attribute description
   * @param  value  attribute value
   */
  public ExtensibleFilter(final String matchingRule, final String type, final byte[] value)
  {
    this(matchingRule, type, value, false);
  }


  /**
   * Creates a new extensible filter.
   *
   * @param  matchingRule  matching rule
   * @param  type  attribute description
   * @param  value  attribute value
   * @param  dnAttrs  DN attributes
   */
  public ExtensibleFilter(final String matchingRule, final String type, final byte[] value, final boolean dnAttrs)
  {
    if (matchingRule == null && type == null) {
      throw new IllegalArgumentException("Either the matching rule or the type must be specified");
    }
    if (value == null) {
      throw new IllegalArgumentException("A match value must be specified");
    }
    matchingRuleID = matchingRule;
    attributeDesc = type;
    assertionValue = value;
    dnAttributes = dnAttrs;
  }


  /**
   * Returns the matching rule id.
   *
   * @return  matching rule id
   */
  public String getMatchingRuleID()
  {
    return matchingRuleID;
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


  /**
   * Returns whether matching should occur against attributes of the DN.
   *
   * @return  whether to match against DN attributes
   */
  public boolean getDnAttributes()
  {
    return dnAttributes;
  }


  @Override
  public DEREncoder getEncoder()
  {
    // CheckStyle:MagicNumber OFF
    final DEREncoder[] encoders = new DEREncoder[4];
    encoders[0] = matchingRuleID != null ? new OctetStringType(new ContextDERTag(1, false), matchingRuleID) : null;
    encoders[1] = attributeDesc != null ? new OctetStringType(new ContextDERTag(2, false), attributeDesc) : null;
    encoders[2] = assertionValue != null ? new OctetStringType(new ContextDERTag(3, false), assertionValue) : null;
    encoders[3] = dnAttributes ? new BooleanType(new ContextDERTag(4, false), true) : null;
    // CheckStyle:MagicNumber ON
    return new ConstructedDEREncoder(
      new ContextDERTag(Filter.Type.EXTENSIBLE_MATCH.ordinal(), true),
      Stream.of(encoders).filter(Objects::nonNull).toArray(DEREncoder[]::new));
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof ExtensibleFilter) {
      final ExtensibleFilter v = (ExtensibleFilter) o;
      return LdapUtils.areEqual(matchingRuleID, v.matchingRuleID) &&
        LdapUtils.areEqual(attributeDesc, v.attributeDesc) &&
        LdapUtils.areEqual(assertionValue, v.assertionValue) &&
        LdapUtils.areEqual(dnAttributes, v.dnAttributes);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(
      HASH_CODE_SEED,
      matchingRuleID,
      attributeDesc,
      assertionValue,
      dnAttributes);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("matchingRuleID=").append(matchingRuleID).append(", ")
      .append("attributeDesc=").append(attributeDesc).append(", ")
      .append("assertionValue=").append(
        assertionValue == null ? null : new String(assertionValue, StandardCharsets.UTF_8)).append(", ")
      .append("dnAttributes=").append(dnAttributes).toString();
  }
}
