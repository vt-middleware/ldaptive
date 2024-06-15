/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Sorts the entries, attributes, and attribute values contained in a search response.
 *
 * @author  Middleware Services
 */
public class SortResultHandler implements SearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 853;


  @Override
  public SearchResponse apply(final SearchResponse response)
  {
    return SearchResponse.sort(response);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof SortResultHandler;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED);
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "]";
  }
}
