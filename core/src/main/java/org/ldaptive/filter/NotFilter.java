/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;

/**
 * Not search filter set defined as:
 *
 * <pre>
 * (!(filter))
 * </pre>
 *
 * @author  Middleware Services
 */
public class NotFilter implements FilterSet
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10079;

  /** Component of this filter. */
  private Filter filterComponent;


  /**
   * Default constructor.
   */
  public NotFilter() {}


  /**
   * Creates a new not filter.
   *
   * @param  component  of this filter
   */
  public NotFilter(final Filter component)
  {
    add(component);
  }


  @Override
  public Type getType()
  {
    return Type.NOT;
  }


  @Override
  public void add(final Filter component)
  {
    if (filterComponent != null) {
      throw new IllegalStateException("Filter component has already been set");
    }
    filterComponent = LdapUtils.assertNotNullArg(component, "Filter component cannot be null");
  }


  /**
   * Returns the component of this filter.
   *
   * @return  filter component
   */
  public Filter getComponent()
  {
    return filterComponent;
  }


  @Override
  public DEREncoder getEncoder()
  {
    LdapUtils.assertNotNullState(filterComponent, "Filter component cannot be null");
    return new ConstructedDEREncoder(
      new ContextDERTag(Type.NOT.ordinal(), true),
      filterComponent.getEncoder());
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof NotFilter) {
      final NotFilter v = (NotFilter) o;
      return LdapUtils.areEqual(filterComponent, v.filterComponent);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, filterComponent);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::filterComponent=" + filterComponent;
  }
}
