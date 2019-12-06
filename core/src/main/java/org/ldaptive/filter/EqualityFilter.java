/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.nio.charset.StandardCharsets;
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
