/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.protocol.SearchFilter;

/**
 * Extensible search filter component.
 *
 * @author  Middleware Services
 */
public class ExtensibleFilter extends AbstractSearchFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10061;

  /** Regex pattern to match this filter type. */
  private static final Pattern FILTER_PATTERN = Pattern.compile(
    "\\((" + SearchFilter.ATTRIBUTE_DESC + ")?(:[Dd][Nn])?(?::(.+))?:=(" + SearchFilter.ASSERTION_VALUE + ")\\)");

  /** Matching rule id. */
  private final String matchingRuleID;

  /** Attribute description. */
  private final String attributeDesc;

  /** Attribute assertion. */
  private final byte[] assertion;

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
    assertion = value;
    dnAttributes = dnAttrs;
  }


  /**
   * Creates a new extensible filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  extensible filter or null if component doesn't match this filter type
   */
  public static ExtensibleFilter parse(final String component)
  {
    final Matcher m = FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      // CheckStyle:MagicNumber OFF
      final String rule = m.group(3);
      final String attr = m.group(1);
      final byte[] value = parseAssertionValue(m.group(4));
      final boolean dn = m.group(2) != null;
      return new ExtensibleFilter(rule, attr, value, dn);
      // CheckStyle:MagicNumber ON
    }
    return null;
  }


  @Override
  public DEREncoder getEncoder()
  {
    // CheckStyle:MagicNumber OFF
    final DEREncoder[] encoders = new DEREncoder[4];
    encoders[0] = matchingRuleID != null ? new OctetStringType(new ContextDERTag(1, false), matchingRuleID) : null;
    encoders[1] = attributeDesc != null ? new OctetStringType(new ContextDERTag(2, false), attributeDesc) : null;
    encoders[2] = assertion != null ? new OctetStringType(new ContextDERTag(3, false), assertion) : null;
    encoders[3] = dnAttributes ? new BooleanType(new ContextDERTag(4, false), true) : null;
    // CheckStyle:MagicNumber ON
    return new ConstructedDEREncoder(
      new ContextDERTag(SearchFilter.Type.EXTENSIBLE_MATCH.ordinal(), true),
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
        LdapUtils.areEqual(assertion, v.assertion) &&
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
      assertion,
      dnAttributes);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("matchingRuleID=").append(matchingRuleID).append(", ")
      .append("attributeDesc=").append(attributeDesc).append(", ")
      .append("assertion=").append(Arrays.toString(assertion)).append(", ")
      .append("dnAttributes=").append(dnAttributes).toString();
  }
}
