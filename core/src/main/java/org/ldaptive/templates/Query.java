/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import org.ldaptive.LdapUtils;

/**
 * Contains data associated with a query request.
 *
 * @author  Middleware Services
 */
public class Query
{

  /** Used for setting empty terms. */
  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** Query separated into terms. */
  private final String[] terms;

  /** Attributes to return with the ldap query. */
  private String[] returnAttributes;

  /** Additional restrictions to place on every query. */
  private String searchRestrictions;

  /** Start index of search results to return. */
  private Integer fromResult;

  /** End index of search results to return. */
  private Integer toResult;


  /**
   * Parses the query from a string into query terms.
   *
   * @param  query  to parse
   */
  public Query(final String query)
  {
    if (query != null) {
      final List<String> l = new ArrayList<>();
      final StringTokenizer queryTokens = new StringTokenizer(LdapUtils.toLowerCase(query).trim());
      while (queryTokens.hasMoreTokens()) {
        l.add(queryTokens.nextToken());
      }
      terms = l.toArray(new String[0]);
    } else {
      terms = EMPTY_STRING_ARRAY;
    }
  }


  /**
   * Returns the terms.
   *
   * @return  query terms
   */
  public String[] getTerms()
  {
    return terms;
  }


  /**
   * Sets the return attributes.
   *
   * @param  attrs  return attributes
   */
  public void setReturnAttributes(final String[] attrs)
  {
    returnAttributes = attrs;
  }


  /**
   * Returns the return attributes.
   *
   * @return  return attributes
   */
  public String[] getReturnAttributes()
  {
    return returnAttributes;
  }


  /**
   * Sets the search restrictions.
   *
   * @param  restrictions  search restrictions
   */
  public void setSearchRestrictions(final String restrictions)
  {
    searchRestrictions = restrictions;
  }


  /**
   * Returns the search restrictions.
   *
   * @return  search restrictions
   */
  public String getSearchRestrictions()
  {
    return searchRestrictions;
  }


  /**
   * Sets the index of the result to begin searching.
   *
   * @param  i  from index
   */
  public void setFromResult(final Integer i)
  {
    fromResult = i;
  }


  /**
   * Returns the from result.
   *
   * @return  from result
   */
  public Integer getFromResult()
  {
    return fromResult;
  }


  /**
   * Sets the index of the result to stop searching.
   *
   * @param  i  to result
   */
  public void setToResult(final Integer i)
  {
    toResult = i;
  }


  /**
   * Returns the to result.
   *
   * @return  to result
   */
  public Integer getToResult()
  {
    return toResult;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "terms=" + Arrays.toString(terms) + ", " +
      "returnAttributes=" + Arrays.toString(returnAttributes) + ", " +
      "searchRestrictions=" + searchRestrictions + ", " +
      "fromResult=" + fromResult + ", " +
      "toResult=" + toResult + "]";
  }
}
