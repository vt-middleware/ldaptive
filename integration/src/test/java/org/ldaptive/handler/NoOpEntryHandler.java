/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Entry handler that does nothing.
 *
 * @author  Middleware Services
 */
public class NoOpEntryHandler implements LdapEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 887;


  @Override
  public LdapEntry apply(final LdapEntry entry)
  {
    return entry;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof NoOpEntryHandler;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, (Object) null);
  }
}
