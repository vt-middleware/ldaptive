/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activates a connection by attempting to open it.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ConnectActivator implements Activator<Connection>
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());


  /** {@inheritDoc} */
  @Override
  public boolean activate(final Connection c)
  {
    boolean success = false;
    if (c != null) {
      try {
        c.open();
        success = true;
      } catch (Exception e) {
        logger.error("unable to connect to the ldap", e);
      }
    }
    return success;
  }
}
