/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Merges the values of the attributes in all entries into a single entry.
 *
 * @author  Miguel Martinez de Espronceda
 */
public class MergeResultHandler extends AbstractEntryHandler<SearchResponse> implements SearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 857;


  /** Default constructor. */
  public MergeResultHandler() {}


  @Override
  public SearchResponse apply(final SearchResponse searchResponse)
  {
    return SearchResponse.merge(searchResponse);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof MergeResultHandler;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(getClass().getName()).append("@").append(hashCode()).append("]").toString();
  }
}
