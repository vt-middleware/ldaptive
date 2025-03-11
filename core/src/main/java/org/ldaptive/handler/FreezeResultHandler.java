/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Freezes a search response so that it can no longer be modified. See {@link org.ldaptive.Freezable}.
 *
 * @author  Middleware Services
 */
public class FreezeResultHandler implements SearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 859;


  @Override
  public SearchResponse apply(final SearchResponse response)
  {
    LdapUtils.assertNotNullArg(response, "Search response cannot be null");
    response.freeze();
    return response;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof FreezeResultHandler;
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
