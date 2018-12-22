/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Approximate search filter component defined as:
 *
 * <pre>
 * (attributeDescription~=attributeValue)
 * </pre>
 *
 * @author  Middleware Services
 */
public class ApproximateFilter extends AbstractAttributeValueAssertionFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10037;

  /** Regex pattern to match this filter type. */
  private static final Pattern FILTER_PATTERN = Pattern.compile(
    "\\((" + ATTRIBUTE_DESC + ")~=(" + ASSERTION_VALUE + ")\\)");


  /**
   * Creates a new approximate filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public ApproximateFilter(final String name, final String value)
  {
    super(Type.APPROXIMATE_MATCH, name, value.getBytes(StandardCharsets.UTF_8));
  }


  /**
   * Creates a new approximate filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public ApproximateFilter(final String name, final byte[] value)
  {
    super(Type.APPROXIMATE_MATCH, name, value);
  }


  /**
   * Creates a new approximate filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  approximate filter or null if component doesn't match this filter type
   */
  public static ApproximateFilter parse(final String component)
  {
    final Matcher m = FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      final String attr = m.group(1);
      final byte[] value = parseAssertionValue(m.group(2));
      return new ApproximateFilter(attr, value);
    }
    return null;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof ApproximateFilter && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeDesc, assertionValue);
  }
}
