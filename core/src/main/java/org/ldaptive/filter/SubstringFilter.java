/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;

/**
 * Substring search filter component defined as:
 *
 * <pre>
 * (attributeDescription=attributeValueWithWildCard)
 * </pre>
 *
 * @author  Middleware Services
 */
public class SubstringFilter implements Filter
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
      encoders[i] = new OctetStringType(
        new ContextDERTag(Substrings.FINAL.ordinal(), false), subFinal);
    }
    return new ConstructedDEREncoder(
      new ContextDERTag(Filter.Type.SUBSTRING.ordinal(), true),
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
      .append("subInitial=").append(
        subInitial == null ? null : new String(subInitial, StandardCharsets.UTF_8)).append(", ")
      .append("subAny=").append(
        subAny == null ? null :
          Stream.of(subAny)
            .map(b -> b == null ? null : new String(b, StandardCharsets.UTF_8))
            .collect(Collectors.toList())).append(", ")
      .append("subFinal=").append(
        subFinal == null ? null : new String(subFinal, StandardCharsets.UTF_8)).toString();
  }
}
