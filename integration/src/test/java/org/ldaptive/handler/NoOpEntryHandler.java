/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapUtils;

/**
 * Entry handler that does nothing.
 *
 * @author  Middleware Services
 */
public class NoOpEntryHandler extends AbstractSearchEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 887;


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, (Object) null);
  }
}
