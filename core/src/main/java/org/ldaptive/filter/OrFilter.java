/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.ContextDERTag;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.NullType;

/**
 * Or search filter set defined as:
 *
 * <pre>
 * (|(filter)(filter)...)
 * </pre>
 *
 * @author  Middleware Services
 */
public class OrFilter implements FilterSet
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10091;

  /** Components of this filter. */
  private final List<Filter> filterComponents = new ArrayList<>();


  /**
   * Default constructor.
   */
  public OrFilter() {}


  /**
   * Creates a new or filter.
   *
   * @param  components  of this filter
   */
  public OrFilter(final Filter... components)
  {
    Stream.of(components).forEach(filterComponents::add);
  }


  @Override
  public Type getType()
  {
    return Type.OR;
  }


  @Override
  public void add(final Filter component)
  {
    filterComponents.add(component);
  }


  @Override
  public DEREncoder getEncoder()
  {
    if (filterComponents.size() == 0) {
      return new NullType(new ContextDERTag(Type.OR.ordinal(), true));
    } else {
      return new ConstructedDEREncoder(
        new ContextDERTag(Type.OR.ordinal(), true),
        filterComponents.stream().map(Filter::getEncoder).toArray(DEREncoder[]::new));
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof OrFilter) {
      final OrFilter v = (OrFilter) o;
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
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("filterComponents=").append(filterComponents).toString();
  }
}
