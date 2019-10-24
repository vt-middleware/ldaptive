/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.Connection;

/**
 * Passivates a connection by attempting to close it.
 *
 * @author  Middleware Services
 */
public class ClosePassivator implements ConnectionPassivator
{


  @Override
  public Boolean apply(final Connection conn)
  {
    if (conn != null) {
      conn.close();
      return true;
    }
    return false;
  }
}
