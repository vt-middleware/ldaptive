/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
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
    return LdapUtils.computeHashCode(HASH_CODE_SEED, filterType, attributeDesc, assertionValue);
  }
}
