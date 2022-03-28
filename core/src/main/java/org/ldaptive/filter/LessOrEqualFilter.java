/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.LdapUtils;

/**
 * Less or equal search filter component defined as:
 *
 * <pre>
   (attributeDescription&lt;=attributeValue)
 * </pre>
 *
 * @author  Middleware Services
 */
public class LessOrEqualFilter extends AbstractAttributeValueAssertionFilter
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10069;


  /**
   * Creates a new less or equal filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public LessOrEqualFilter(final String name, final String value)
  {
    super(Type.LESS_OR_EQUAL, name, LdapUtils.utf8Encode(value, false));
  }


  /**
   * Creates a new less or equal filter.
   *
   * @param  name  attribute description
   * @param  value  attribute value
   */
  public LessOrEqualFilter(final String name, final byte[] value)
  {
    super(Type.LESS_OR_EQUAL, name, value);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof LessOrEqualFilter && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, filterType, attributeDesc, assertionValue);
  }
}
