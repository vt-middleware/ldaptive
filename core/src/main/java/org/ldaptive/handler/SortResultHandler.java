/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Sorts the entries, attributes, and attribute values contained in a search response.
 *
 * @author  Middleware Services
 */
public class SortResultHandler extends AbstractSearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 853;


  /** Default constructor. */
  public SortResultHandler()
  {
    this(Usage.SYNC);
  }


  /**
   * Creates a new sort result handler.
   *
   * @param  u  handler usage
   */
  public SortResultHandler(final Usage u)
  {
    super(u);
  }


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
    if (o instanceof SortResultHandler) {
      final SortResultHandler v = (SortResultHandler) o;
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
