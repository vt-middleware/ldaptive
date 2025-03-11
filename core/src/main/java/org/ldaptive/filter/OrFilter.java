/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    filterComponents.addAll(
      Arrays.asList(
        LdapUtils.assertNotContainsNullArgOr(
          components,
          Objects::isNull,
          "Filter components cannot be null or contain null")));
  }


  @Override
  public Type getType()
  {
    return Type.OR;
  }


  @Override
  public void add(final Filter component)
  {
    filterComponents.add(LdapUtils.assertNotNullArg(component, "Filter component cannot be null"));
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
    LdapUtils.assertNotContainsNullArg(filterComponents, "Filter components cannot contain null");
    if (filterComponents.isEmpty()) {
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
    return getClass().getName() + "@" + hashCode() + "::filterComponents=" + filterComponents;
  }
}
