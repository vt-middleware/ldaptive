/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ldaptive.LdapUtils;

/**
 * Equality search filter component.
 *
 * @author  Middleware Services
 */
public class EqualityFilter extends AbstractAttributeValueAssertionFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10039;

  /** Regex pattern to match this filter type. */
  private static final Pattern FILTER_PATTERN = Pattern.compile("\\((" + ATTRIBUTE_DESC + ")=([^\\*]*)\\)");


  /**
   * Creates a new equality filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public EqualityFilter(final String name, final String value)
  {
    super(Type.EQUALITY, name, value.getBytes(StandardCharsets.UTF_8));
  }


  /**
   * Creates a new equality filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public EqualityFilter(final String name, final byte[] value)
  {
    super(Type.EQUALITY, name, value);
  }


  /**
   * Creates a new equality filter by parsing the supplied filter string.
   *
   * @param  component  to parse
   *
   * @return  equality filter or null if component doesn't match this filter type
   */
  public static EqualityFilter parse(final String component)
  {
    final Matcher m = FILTER_PATTERN.matcher(component);
    if (m.matches()) {
      final String attr = m.group(1);
      final byte[] value = parseAssertionValue(m.group(2));
      return new EqualityFilter(attr, value);
    }
    return null;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof EqualityFilter && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeDesc, assertionValue);
  }
}
