/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;

/**
 * Merges the values of the attributes in all entries into a single entry.
 *
 * @author  Miguel Martinez de Espronceda
 */
public class MergeResultHandler extends AbstractSearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 857;


  /** Default constructor. */
  public MergeResultHandler()
  {
    this(Usage.SYNC);
  }


  /**
   * Creates a new merge result handler.
   *
   * @param  u  handler usage
   */
  public MergeResultHandler(final Usage u)
  {
    super(u);
  }


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
        for (LdapAttribute la : entry.getAttributes()) {
          final LdapAttribute oldAttr = mergedEntry.getAttribute(la.getName());
          if (oldAttr == null) {
            mergedEntry.addAttributes(LdapAttribute.copy(la));
          } else {
            if (oldAttr.isBinary()) {
              oldAttr.addBinaryValues(la.getBinaryValues());
            } else {
              oldAttr.addStringValues(la.getStringValues());
            }
          }
        }
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
    if (o instanceof MergeResultHandler) {
      final MergeResultHandler v = (MergeResultHandler) o;
      return LdapUtils.areEqual(getUsage(), v.getUsage());
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getUsage());
  }


  @Override
  public String toString()
  {
    return "[" + super.toString() + "]";
  }
}
