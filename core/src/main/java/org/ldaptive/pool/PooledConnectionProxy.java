/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.lang.reflect.InvocationHandler;
import java.time.Instant;
import org.ldaptive.Connection;

/**
 * Provides an interface for metadata surrounding a connection that is participating in the connection pool.
 *
 * @author  Middleware Services
 */
public interface PooledConnectionProxy extends InvocationHandler
{


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
  Instant getCreatedTime();


  /**
   * Returns the statistics associated with this connection's activity in the pool.
   *
   * @return  pooled connection statistics
   */
  PooledConnectionStatistics getPooledConnectionStatistics();
}
