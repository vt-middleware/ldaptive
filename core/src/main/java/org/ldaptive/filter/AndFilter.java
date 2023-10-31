/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.NullType;

/**
 * And search filter set defined as:
 *
 * <pre>
   (&amp;(filter)(filter)...)
 * </pre>
 *
 * @author  Middleware Services
 */
public class AndFilter implements FilterSet
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10009;

  /** Components of this filter. */
  private final List<Filter> filterComponents = new ArrayList<>();


  /**
   * Default constructor.
   */
  public AndFilter() {}


  /**
   * Creates a new and filter.
   *
   * @param  components  of this filter
   */
  public AndFilter(final Filter... components)
  {
    filterComponents.addAll(Arrays.asList(components));
  }


  @Override
  public Type getType()
  {
    return Type.AND;
  }


  @Override
  public void add(final Filter component)
  {
    filterComponents.add(component);
  }


  /**
   * Returns the components of this filter.
   *
   * @return  filter components
   */
  public List<Filter> getComponents()
  {
    return Collections.unmodifiableList(filterComponents);
  }


  @Override
  public DEREncoder getEncoder()
  {
    if (filterComponents.size() == 0) {
      return new NullType(new ContextDERTag(Type.AND.ordinal(), true));
    } else {
      return new ConstructedDEREncoder(
        new ContextDERTag(Type.AND.ordinal(), true),
        filterComponents.stream().map(Filter::getEncoder).toArray(DEREncoder[]::new));
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AndFilter) {
      final AndFilter v = (AndFilter) o;
      return LdapUtils.areEqual(filterComponents, v.filterComponents);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, filterComponents);
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::filterComponents=" + filterComponents;
  }
}
