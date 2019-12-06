/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Comparator;

/**
 * Compares two ldap results, ignoring the case of supplied attribute values.
 *
 * @author  Middleware Services
 */
public class SearchResultIgnoreCaseComparator implements Comparator<SearchResponse>
{

  /** names of attributes to ignore the case of. */
  private final String[] attributeNames;


  /** Creates a new ldap result ignore case comparator that will ignore the case of all attribute values. */
  public SearchResultIgnoreCaseComparator()
  {
    this((String[]) null);
  }


  /**
   * Creates a new ldap result ignore case comparator with the supplied names. If names is null all attributes will be
   * case ignored.
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
   * @return  a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
   *          than the second.
   */
  public int compare(final SearchResponse a, final SearchResponse b)
  {
    return lowerCaseResult(a, attributeNames).hashCode() - lowerCaseResult(b, attributeNames).hashCode();
  }


  /**
   * Returns a new ldap result whose attribute values have been lower cased as configured.
   *
   * @param  lr  result to copy values from
   * @param  names  of attributes whose case should be ignored
   *
   * @return  ldap result with lower cased attribute values
   *
   * @throws  IllegalArgumentException  if a binary attribute is lower cased
   */
  public static SearchResponse lowerCaseResult(final SearchResponse lr, final String... names)
  {
    final SearchResponse lowerCase = new SearchResponse();
    for (LdapEntry le : lr.getEntries()) {
      lowerCase.addEntries(LdapEntryIgnoreCaseComparator.lowerCaseEntry(le, names));
    }
    return lowerCase;
  }
}
