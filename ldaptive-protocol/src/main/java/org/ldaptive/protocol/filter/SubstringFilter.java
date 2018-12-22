/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.protocol.SearchFilter;

/**
 * Substring search filter component defined as:
 *
 * <pre>
 * (attributeDescription=attributeValueWithWildCard)
 * </pre>
 *
 * @author  Middleware Services
 */
public class SubstringFilter extends AbstractSearchFilter
{

  /** Type of substring match. */
  public enum Substrings {

    /** Initial substring. */
    INITIAL,

    /** Any substring. */
    ANY,

    /** Final substring. */
    FINAL,
  }

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10099;

  /** Regex pattern to match this filter type. */
  private static final Pattern FILTER_PATTERN = Pattern.compile(
    "\\((" + SearchFilter.ATTRIBUTE_DESC + ")=((?:[^\\*]*\\*[^\\*]*)+)\\)");

  /** Attribute description. */
  private final String attributeDesc;

  /** Substring initial. */
  private final byte[] subInitial;

  /** Substring any .*/
  private final byte[][] subAny;

  /** Substring final. */
  private final byte[] subFinal;


  /**
   * Creates a new substring filter.
   *
   * @param  name  attribute description
   * @param  startsWith  substring initial
   * @param  endsWith  substring final
   * @param  contains  substring any
   */
  public SubstringFilter(final String name, final String startsWith, final String endsWith, final String... contains)
  {
    if (startsWith == null && endsWith == null && contains == null) {
      throw new IllegalArgumentException("Assertion must have one of subInitial, subAny, or subFinal");
    }
    attributeDesc = name;
    byte[][] containsBytes = null;
    if (contains != null) {
      containsBytes = new byte[contains.length][];
      for (int i = 0; i < contains.length; i++) {
        containsBytes[i] = contains[i].getBytes(StandardCharsets.UTF_8);
      }
    }
    subInitial = startsWith != null ? startsWith.getBytes(StandardCharsets.UTF_8) : null;
    subAny = containsBytes;
    subFinal = endsWith != null ? endsWith.getBytes(StandardCharsets.UTF_8) : null;
  }


  /**
   * Creates a new substring filter.
   *
   * @param  name  attribute description
   * @param  startsWith  substring initial
   * @param  endsWith  substring final
   * @param  contains  substring any
   */
  public SubstringFilter(final String name, final byte[] startsWith, final byte[] endsWith, final byte[]... contains)
  {
    if (startsWith == null && endsWith == null && contains == null) {
      throw new IllegalArgumentException("Assertion must have one of subInitial, subAny, or subFinal");
    }
    attributeDesc = name;
    subInitial = startsWith;
    subAny = contains;
    subFinal = endsWith;
  }


  /**
   * Creates a new substring filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  substring filter or null if component doesn't match this filter type
   */
  public static SubstringFilter parse(final String component)
  {
    final Matcher m = FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      // don't allow presence match or multiple asterisks
      if (!m.group(2).equals("*") && !m.group(2).contains("**")) {
        final String attr = m.group(1);
        final String assertions = m.group(2);

        String startsWith = null;
        final int firstAsterisk = assertions.indexOf('*');
        if (firstAsterisk > 0) {
          startsWith = assertions.substring(0, firstAsterisk);
        }
        String endsWith = null;
        final int lastAsterisk = assertions.lastIndexOf('*');
        if (lastAsterisk < assertions.length() - 1) {
          endsWith = assertions.substring(lastAsterisk + 1);
        }
        String[] contains = null;
        if (lastAsterisk > firstAsterisk) {
          contains = assertions.substring(firstAsterisk + 1, lastAsterisk).split("\\*");
        }
        return new SubstringFilter(
          attr,
          startsWith != null ? parseAssertionValue(startsWith) : null,
          endsWith != null ? parseAssertionValue(endsWith) : null,
          contains != null ? parseAssertionValue(contains) : null);
      }
    }
    return null;
  }


  /**
   * Returns the number of assertions in this substring filter.
   *
   * @return  assesrtion count
   */
  private int getAssertionCount()
  {
    int count = subAny != null ? subAny.length : 0;
    if (subInitial != null) {
      count++;
    }
    if (subFinal != null) {
      count++;
    }
    return count;
  }


  @Override
  public DEREncoder getEncoder()
  {
    final DEREncoder[] encoders = new DEREncoder[getAssertionCount()];
    int i = 0;
    if (subInitial != null) {
      encoders[i++] = new OctetStringType(
        new ContextDERTag(Substrings.INITIAL.ordinal(), false), subInitial);
    }
    if (subAny != null && subAny.length > 0) {
      for (byte[] assertion : subAny) {
        encoders[i++] = new OctetStringType(new ContextDERTag(Substrings.ANY.ordinal(), false), assertion);
      }
    }
    if (subFinal != null) {
      encoders[i++] = new OctetStringType(
        new ContextDERTag(Substrings.FINAL.ordinal(), false), subFinal);
    }
    return new ConstructedDEREncoder(
      new ContextDERTag(SearchFilter.Type.SUBSTRING.ordinal(), true),
      new OctetStringType(attributeDesc),
      new ConstructedDEREncoder(
        UniversalDERTag.SEQ,
        encoders));
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SubstringFilter) {
      final SubstringFilter v = (SubstringFilter) o;
      return LdapUtils.areEqual(attributeDesc, v.attributeDesc) &&
        LdapUtils.areEqual(subInitial, v.subInitial) &&
        LdapUtils.areEqual(subAny, v.subAny) &&
        LdapUtils.areEqual(subFinal, v.subFinal);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(
      HASH_CODE_SEED,
      attributeDesc,
      subInitial,
      subAny,
      subFinal);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("attributeDesc=").append(attributeDesc).append(", ")
      .append("subInitial=").append(Arrays.toString(subInitial)).append(", ")
      .append("subAny=").append(Arrays.toString(subAny)).append(", ")
      .append("subFinal=").append(Arrays.toString(subFinal)).toString();
  }
}
