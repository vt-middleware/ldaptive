/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.lang.reflect.InvocationHandler;
import org.ldaptive.Connection;

/**
 * Provides an interface for metadata surrounding a connection that is
 * participating in the connection pool.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface PooledConnectionProxy extends InvocationHandler
{


  /**
   * Returns the connection pool that this proxy is participating in.
   *
   * @return  connection pool
   */
  ConnectionPool getConnectionPool();


  /**
   * Returns the connection that is being proxied.
   *
   * @return  underlying connection
   */
  Connection getConnection();


  /**
   * Returns the time this proxy was created.
   *
   * @return  creation timestamp in milliseconds
   */
  long getCreatedTime();


  /**
   * Returns the statistics associated with this connection's activity in the
   * pool.
   *
   * @return  pooled connection statistics
   */
  PooledConnectionStatistics getPooledConnectionStatistics();
}
