/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

/**
 * Base implementation to parse an LDAP search filter string.
 *
 * @author  Middleware Services
 */
public abstract class AbstractFilterFunction implements FilterFunction
{


  @Override
  public Filter apply(final String filter)
  {
    final String balancedFilter;
    // Check for balanced parentheses
    if (filter.startsWith("(")) {
      if (!filter.endsWith(")")) {
        throw new IllegalArgumentException("Unbalanced parentheses. Opening paren without closing paren.");
      }
      balancedFilter = filter;
    } else if (filter.endsWith(")")) {
      throw new IllegalArgumentException("Unbalanced parentheses. Closing paren without opening paren.");
    } else {
      // Allow entire filter strings without enclosing parentheses
      balancedFilter = "(".concat(filter).concat(")");
    }

    return readNextComponent(balancedFilter);
  }


  /**
   * Reads the next component contained in the supplied filter.
   *
   * @param  filter  to parse
   *
   * @return  search filter
   *
   * @throws  IllegalArgumentException  if filter does not start with '(' and end with ')'
   */
  private Filter readNextComponent(final String filter)
  {
    final int end = filter.length() - 1;
    if (filter.charAt(0) != '(' || filter.charAt(end) != ')') {
      throw new IllegalArgumentException("Filter must be surround by parentheses: " + filter);
    }
    int pos = 1;
    final Filter searchFilter;
    switch (filter.charAt(pos)) {

    case '&':
      searchFilter = readFilterSet(new AndFilter(), filter, ++pos, end);
      break;

    case '|':
      searchFilter = readFilterSet(new OrFilter(), filter, ++pos, end);
      break;

    case '!':
      searchFilter = readFilterSet(new NotFilter(), filter, ++pos, end);
      break;

    default:
      // attempt to match a non-set filter type
      searchFilter = parseFilterComp(filter);
      if (searchFilter == null) {
        throw new IllegalArgumentException("Could not parse filter: " + filter);
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
   *
   * @return  the supplied filter set with components added from filter
   *
   * @throws  IllegalArgumentException  if filter doesn't start with '(' and containing a matching ')'
   */
  private FilterSet readFilterSet(final FilterSet set, final String filter, final int start, final int end)
  {
    int pos = start;
    int closeIndex = findMatchingParenPosition(filter, pos);
    if (filter.charAt(pos) != '(' || closeIndex == -1 || closeIndex == end) {
      throw new IllegalArgumentException(
        "Invalid filter syntax, missing parenthesis after " + set.getType());
    }
    while (pos < end) {
      set.add(readNextComponent(filter.substring(pos, closeIndex + 1)));
      pos = closeIndex + 1;
      if (pos < end) {
        closeIndex = findMatchingParenPosition(filter, pos);
      }
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
   * @throws  IllegalArgumentException  if filter is null, empty or does not begin with '('
   */
  private int findMatchingParenPosition(final String filter, final int start)
  {
    if (filter == null || filter.length() == 0) {
      throw new IllegalArgumentException("Filter cannot be null or empty");
    }
    if (filter.charAt(start) != '(') {
      throw new IllegalArgumentException("Filter must begin with '('");
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
   */
  protected abstract Filter parseFilterComp(String filter);
}
