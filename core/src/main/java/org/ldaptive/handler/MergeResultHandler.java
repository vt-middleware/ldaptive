/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;

/**
 * Merges the values of the attributes in all entries into a single entry.
 *
 * @author  Miguel Martinez de Espronceda
 */
public class MergeResultHandler implements SearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 857;


  @Override
  public SearchResponse apply(final SearchResponse searchResponse)
  {
    return merge(searchResponse);
  }


  /**
   * Merges the entries in the supplied result into a single entry. This method always returns a search result of size
   * zero or one.
   *
   * @param  searchResponse  search result containing entries to merge
   *
   * @return  search result containing a single merged entry
   */
  private SearchResponse merge(final SearchResponse searchResponse)
  {
    final SearchResponse merged = SearchResponse.builder().copy(searchResponse).build();

    LdapEntry mergedEntry = null;
    for (LdapEntry entry : searchResponse.getEntries()) {
      if (mergedEntry == null) {
        mergedEntry = LdapEntry.copy(entry);
      } else {
        mergedEntry.mergeAttributes(entry.getAttributes());
      }
    }
    if (mergedEntry != null) {
      merged.addEntries(mergedEntry);
    }

    SearchResultReference mergedReference = null;
    for (SearchResultReference reference : searchResponse.getReferences()) {
      if (mergedReference == null) {
        mergedReference = SearchResultReference.copy(reference);
      } else {
        mergedReference.addUris(reference.getUris());
      }
    }
    if (mergedReference != null) {
      merged.addReferences(mergedReference);
    }
    return merged;
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
    return "[" + getClass().getName() + "@" + hashCode() + "]";
  }
}
