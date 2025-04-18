/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.ResultCode;

/**
 * Base implementation to parse an LDAP search filter string.
 *
 * @author  Middleware Services
 */
public abstract class AbstractFilterFunction implements FilterFunction
{

  /** Maximum filter depth. */
  private static final int MAX_FILTER_DEPTH = 100;


  @Override
  public Filter parse(final String filter)
    throws FilterParseException
  {
    if (filter == null) {
      throw new IllegalArgumentException("Filter cannot be null");
    }
    final String balancedFilter;
    // Check for balanced parentheses
    if (filter.startsWith("(")) {
      if (!filter.endsWith(")")) {
        throw new FilterParseException(
          ResultCode.FILTER_ERROR,
          "Unbalanced parentheses. Opening paren without closing paren.");
      }
      balancedFilter = filter;
    } else if (filter.endsWith(")")) {
      throw new FilterParseException(
        ResultCode.FILTER_ERROR,
        "Unbalanced parentheses. Closing paren without opening paren.");
    } else {
      // Allow entire filter strings without enclosing parentheses
      balancedFilter = "(".concat(filter).concat(")");
    }

    try {
      return readNextComponent(balancedFilter, 0);
    } catch (FilterParseException e) {
      throw e;
    } catch (Exception e) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, e);
    }
  }


  /**
   * Reads the next component contained in the supplied filter.
   *
   * @param  filter  to parse
   * @param  depth  counter to track invocation depth
   *
   * @return  search filter
   *
   * @throws  FilterParseException  if filter does not start with '(' and end with ')'
   */
  private Filter readNextComponent(final String filter, final int depth)
    throws FilterParseException
  {
    if (depth > MAX_FILTER_DEPTH) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter parse depth exceeded");
    }
    final int end = filter.length() - 1;
    if (filter.charAt(0) != '(' || filter.charAt(end) != ')') {
      throw new FilterParseException(
        ResultCode.FILTER_ERROR,
        "Filter must be surround by parentheses: '" + filter + "'");
    }
    int pos = 1;
    final Filter searchFilter;
    switch (filter.charAt(pos)) {

    case '&':
      searchFilter = readFilterSet(new AndFilter(), filter, ++pos, end, depth);
      break;

    case '|':
      searchFilter = readFilterSet(new OrFilter(), filter, ++pos, end, depth);
      break;

    case '!':
      searchFilter = readFilterSet(new NotFilter(), filter, ++pos, end, depth);
      break;

    default:
      // attempt to match a non-set filter type
      searchFilter = parseFilterComp(filter);
      if (searchFilter == null) {
        throw new FilterParseException(ResultCode.FILTER_ERROR, "Could not determine filter type for '" + filter + "'");
      }
      break;
    }
    return searchFilter;
  }


  /**
   * Reads the supplied filter using the supplied indices and adds them to the supplied filter set.
   *
   * @param  set  to update
   * @param  filter  to parse
   * @param  start  position in filter
   * @param  end  position in filter
   * @param  depth  counter to track invocation depth
   *
   * @return  the supplied filter set with components added from filter
   *
   * @throws  FilterParseException  if filter doesn't start with '(' and containing a matching ')'
   */
  private FilterSet readFilterSet(
    final FilterSet set,
    final String filter,
    final int start,
    final int end,
    final int depth)
    throws FilterParseException
  {
    int pos = start;
    int closeIndex = findMatchingParenPosition(filter, pos);
    if (filter.charAt(pos) != '(' || closeIndex == -1 || closeIndex == end) {
      throw new FilterParseException(
        ResultCode.FILTER_ERROR,
        "Invalid filter syntax, missing parenthesis after " + set.getType());
    }
    while (pos < end) {
      set.add(readNextComponent(filter.substring(pos, closeIndex + 1), depth + 1));
      pos = closeIndex + 1;
      if (pos < end) {
        closeIndex = findMatchingParenPosition(filter, pos);
      }
    }
    if (pos > end) {
      throw new FilterParseException(
        ResultCode.FILTER_ERROR,
        "Invalid filter syntax, missing parenthesis after " + set.getType());
    }
    return set;
  }


  /**
   * Returns the index in the supplied filter of the closing paren that matches the opening paren at the start of the
   * filter.
   *
   * @param  filter  to search
   * @param  start  position of the opening paren
   *
   * @return  index of the matching paren
   *
   * @throws  FilterParseException  if filter is null, empty or does not begin with '('
   */
  private int findMatchingParenPosition(final String filter, final int start)
    throws FilterParseException
  {
    if (filter == null || filter.isEmpty()) {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter cannot be null or empty");
    }
    if (filter.charAt(start) != '(') {
      throw new FilterParseException(ResultCode.FILTER_ERROR, "Filter must begin with '('");
    }
    int pos = start + 1;
    int parenCount = 1;
    while (pos < filter.length()) {
      final char c = filter.charAt(pos);
      if (c == '(') {
        parenCount++;
      } else if (c == ')') {
        parenCount--;
      }
      if (parenCount == 0) {
        return pos;
      }
      pos++;
    }
    return -1;
  }


  /**
   * Inspects the supplied filter string and creates the type of filter it represents.
   *
   * @param  filter  to inspect
   *
   * @return  search filter
   *
   * @throws  FilterParseException  if filter is invalid
   */
  protected abstract Filter parseFilterComp(String filter)
    throws FilterParseException;
}
