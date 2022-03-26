/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
import org.ldaptive.LdapUtils;

/**
 * Greater or equal search filter component defined as:
 *
 * <pre>
   (attributeDescription&gt;=attributeValue)
 * </pre>
 *
 * @author  Middleware Services
 */
public class GreaterOrEqualFilter extends AbstractAttributeValueAssertionFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10067;


  /**
   * Creates a new greater or equal filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public GreaterOrEqualFilter(final String name, final String value)
  {
    super(Type.GREATER_OR_EQUAL, name, value.getBytes(StandardCharsets.UTF_8));
  }


  /**
   * Creates a new greater or equal filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public GreaterOrEqualFilter(final String name, final byte[] value)
  {
    super(Type.GREATER_OR_EQUAL, name, value);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof GreaterOrEqualFilter && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, filterType, attributeDesc, assertionValue);
  }
}
