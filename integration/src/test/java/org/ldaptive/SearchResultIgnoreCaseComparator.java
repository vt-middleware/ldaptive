/*
  $Id$

  Copyright (C) 2003-2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive;

import java.util.Comparator;

/**
 * Compares two ldap results, ignoring the case of supplied attribute values.
 *
 * @author  Middleware Services
 * @version  $Revision$ $Date$
 */
public class SearchResultIgnoreCaseComparator implements Comparator<SearchResult>
{

  /** names of attributes to ignore the case of. */
  private final String[] attributeNames;


  /**
   * Creates a new ldap result ignore case comparator that will ignore the
   * case of all attribute values.
   */
  public SearchResultIgnoreCaseComparator()
  {
    this((String[]) null);
  }


  /**
   * Creates a new ldap result ignore case comparator with the supplied names.
   * If names is null all attributes will be case ignored.
   *
   * @param  names  of attributes whose case should be ignored
   */
  public SearchResultIgnoreCaseComparator(final String... names)
  {
    attributeNames = names;
  }


  /**
   * Compares two ldap results.
   *
   * @param  a  first result for the comparison
   * @param  b  second result for the comparison
   *
   * @return  a negative integer, zero, or a positive integer as the first
   * argument is less than, equal to, or greater than the second.
   */
  public int compare(final SearchResult a, final SearchResult b)
  {
    return lowerCaseResult(a, attributeNames).hashCode() -
           lowerCaseResult(b, attributeNames).hashCode();
  }


  /**
   * Returns a new ldap result whose attribute values have been lower cased as
   * configured.
   *
   * @param  lr  result to copy values from
   * @param  names  of attributes whose case should be ignored
   *
   * @return  ldap result with lower cased attribute values
   *
   * @throws  IllegalArgumentException  if a binary attribute is lower cased
   */
  public static SearchResult lowerCaseResult(
    final SearchResult lr, final String... names)
  {
    final SearchResult lowerCase = new SearchResult(lr.getSortBehavior());
    for (LdapEntry le : lr.getEntries()) {
      lowerCase.addEntry(
        LdapEntryIgnoreCaseComparator.lowerCaseEntry(le, names));
    }
    return lowerCase;
  }
}
