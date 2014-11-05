/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.Connection;

/**
 * Passivates a connection by attempting to close it.
 *
 * @author  Middleware Services
 */
public class ClosePassivator implements Passivator<Connection>
{


  /** {@inheritDoc} */
  @Override
  public boolean passivate(final Connection c)
  {
    boolean success = false;
    if (c != null) {
      c.close();
      success = true;
    }
    return success;
  }
}
