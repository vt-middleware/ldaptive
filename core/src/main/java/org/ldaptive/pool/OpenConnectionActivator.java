/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activates a connection by attempting to open it.
 *
 * @author  Middleware Services
 */
public class OpenConnectionActivator implements ConnectionActivator
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());


  @Override
  public Boolean apply(final Connection conn)
  {
    if (conn != null) {
      try {
        if (!conn.isOpen()) {
          conn.open();
        }
        return true;
      } catch (Exception e) {
        logger.error("unable to connect to the ldap", e);
      }
    }
    return false;
  }
}
