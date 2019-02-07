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
public class ConnectActivator implements Activator<Connection>
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());


  @Override
  public boolean activate(final Connection c)
  {
    if (c != null) {
      try {
        c.open();
        return true;
      } catch (Exception e) {
        logger.error("unable to connect to the ldap", e);
      }
    }
    return false;
  }
}
