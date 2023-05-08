/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.Arrays;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.filter.ExtensibleFilter;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.FilterParseException;
import org.ldaptive.filter.FilterParser;
import org.ldaptive.filter.FilterSet;

/**
 * Request control for limiting the attribute values returned by a search request.
 * See https://tools.ietf.org/html/rfc3876. Control is defined as:
 *
 * <pre>
    ValuesReturnFilter ::= SEQUENCE OF SimpleFilterItem

    SimpleFilterItem ::= CHOICE {
       equalityMatch   [3] AttributeValueAssertion,
       substrings      [4] SubstringFilter,
       greaterOrEqual  [5] AttributeValueAssertion,
       lessOrEqual     [6] AttributeValueAssertion,
       present         [7] AttributeDescription,
       approxMatch     [8] AttributeValueAssertion,
       extensibleMatch [9] SimpleMatchingAssertion }

    SimpleMatchingAssertion ::= SEQUENCE {
       matchingRule    [1] MatchingRuleId OPTIONAL,
       type            [2] AttributeDescription OPTIONAL,
       --- at least one of the above must be present
       matchValue      [3] AssertionValue}
 * </pre>
 *
 * @author  Middleware Services
 */
public class MatchedValuesRequestControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.826.0.1.3344810.2.3";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7057;

  /** list of matched values filters. */
  private Filter[] matchedValuesFilters;


  /** Default constructor. */
  public MatchedValuesRequestControl()
  {
    super(OID);
  }


  /**
   * Creates a new matched values request control.
   *
   * @param  filters  to use for value matching
   */
  public MatchedValuesRequestControl(final String... filters)
  {
    this(filters, false);
  }


  /**
   * Creates a new matched values request control.
   *
   * @param  filters  to use for value matching
   * @param  critical  whether this control is critical
   */
  public MatchedValuesRequestControl(final String[] filters, final boolean critical)
  {
    super(OID, critical);
    setMatchedValuesFilters(filters);
  }


  /**
   * Creates a new matched values request control.
   *
   * @param  filters  to use for value matching
   */
  public MatchedValuesRequestControl(final Filter... filters)
  {
    this(filters, false);
  }


  /**
   * Creates a new matched values request control.
   *
   * @param  filters  to use for value matching
   * @param  critical  whether this control is critical
   */
  public MatchedValuesRequestControl(final Filter[] filters, final boolean critical)
  {
    super(OID, critical);
    setMatchedValuesFilters(filters);
  }


  @Override
  public boolean hasValue()
  {
    return true;
  }


  /**
   * Returns the filters to use for matching values.
   *
   * @return  matched values filters
   */
  public Filter[] getMatchedValuesFilters()
  {
    return matchedValuesFilters;
  }


  /**
   * Sets the filters to use for matching values.
   *
   * @param  filters  for matching values
   *
   * @throws  IllegalArgumentException  if the filter cannot be parsed or is not allowed
   */
  public void setMatchedValuesFilters(final String... filters)
  {
    final Filter[] parsedFilters = new Filter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      try {
        parsedFilters[i] = FilterParser.parse(filters[i]);
      } catch (FilterParseException e) {
        throw new IllegalArgumentException(e);
      }
    }
    setMatchedValuesFilters(parsedFilters);
  }


  /**
   * Sets the filters to use for matching values.
   *
   * @param  filters  for matching values
   *
   * @throws IllegalArgumentException  if the filter is not allowed
   */
  public void setMatchedValuesFilters(final Filter... filters)
  {
    for (Filter filter : filters) {
      validateFilter(filter);
    }
    matchedValuesFilters = filters;
  }


  /**
   * Throws if the supplied filter is not a valid type for the matched values request control.
   *
   * @param  filter  to validate
   *
   * @throws IllegalArgumentException  if the filter is null or not a valid type
   */
  private void validateFilter(final Filter filter)
  {
    if (filter == null) {
      throw new IllegalArgumentException("Filter cannot be null");
    } else if (filter instanceof FilterSet) {
      throw new IllegalArgumentException(
        "MatchedValuesRequestControl does not support AND, OR and NOT filter types");
    } else if (filter instanceof ExtensibleFilter && ((ExtensibleFilter) filter).getDnAttributes()) {
      throw new IllegalArgumentException(
        "MatchedValuesRequestControl does not support an extensible filter with dnAttributes");
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof MatchedValuesRequestControl && super.equals(o)) {
      final MatchedValuesRequestControl v = (MatchedValuesRequestControl) o;
      return LdapUtils.areEqual(matchedValuesFilters, v.matchedValuesFilters);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getOID(),
        getCriticality(),
        matchedValuesFilters);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "criticality=" + getCriticality() + ", " +
      "matchedValuesFilters=" + Arrays.toString(matchedValuesFilters) + "]";
  }


  @Override
  public byte[] encode()
  {
    final ConstructedDEREncoder se = new ConstructedDEREncoder(
      UniversalDERTag.SEQ,
      Stream.of(matchedValuesFilters).map(Filter::getEncoder).toArray(DEREncoder[]::new));
    return se.encode();
  }
}
