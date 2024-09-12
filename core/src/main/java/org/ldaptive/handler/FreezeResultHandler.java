/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Freezes a search response so that it can no longer be modified. See {@link org.ldaptive.Freezable}.
 *
 * @author  Middleware Services
 */
public class FreezeResultHandler extends AbstractSearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 859;


  /** Default constructor. */
  public FreezeResultHandler()
  {
    this(Usage.SYNC);
  }


  /**
   * Creates a new freeze result handler.
   *
   * @param  u  handler usage
   */
  public FreezeResultHandler(final Usage u)
  {
    super(u);
  }


  @Override
  public SearchResponse apply(final SearchResponse response)
  {
    response.freeze();
    return response;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof FreezeResultHandler) {
      final FreezeResultHandler v = (FreezeResultHandler) o;
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
