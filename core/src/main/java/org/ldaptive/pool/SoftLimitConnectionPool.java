/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.NoSuchElementException;
import org.ldaptive.Connection;
import org.ldaptive.DefaultConnectionFactory;

/**
 * Implements a pool of connections that has a set minimum and maximum size. The pool will grow beyond it's maximum size
 * as necessary based on it's current load. Pool size will return to it's minimum based on the configuration of the
 * prune strategy. See {@link PruneStrategy}. This implementation should be used when you have some flexibility in the
 * number of connections that can be created to handle spikes in load. See {@link AbstractConnectionPool}. Note that
 * this pool will begin blocking if it cannot create new connections.
 *
 * @author  Middleware Services
 */
public class SoftLimitConnectionPool extends BlockingConnectionPool
{


  /** Creates a new soft limit pool. */
  public SoftLimitConnectionPool() {}


  /**
   * Creates a new soft limit pool.
   *
   * @param  cf  connection factory
   */
  public SoftLimitConnectionPool(final DefaultConnectionFactory cf)
  {
    super(new PoolConfig(), cf);
  }


  /**
   * Creates a new soft limit pool.
   *
   * @param  pc  pool configuration
   * @param  cf  connection factory
   */
  public SoftLimitConnectionPool(final PoolConfig pc, final DefaultConnectionFactory cf)
  {
    super(pc, cf);
  }


  @Override
  public Connection getConnection()
    throws PoolException
  {
    isInitialized();

    PooledConnectionProxy pc = null;
    logger.trace("waiting on pool lock for check out {}", poolLock.getQueueLength());
    poolLock.lock();
    try {
      // if an available connection exists, use it
      // if no available connections, attempt to create
      if (!available.isEmpty()) {
        try {
          logger.trace("retrieve available connection");
          pc = retrieveAvailableConnection();
        } catch (NoSuchElementException e) {
          logger.error("could not remove connection from list", e);
          throw new IllegalStateException("Pool is empty", e);
        }
      }
    } finally {
      poolLock.unlock();
    }

    if (pc == null) {
      // no connection was available, create a new one
      pc = createActiveConnection();
      if (pc == null) {
        if (available.isEmpty() && active.isEmpty()) {
          logger.error("Could not service check out request");
          throw new PoolExhaustedException("Pool is empty and connection creation failed");
        }
        logger.debug("create failed, block until a connection is available");
        pc = blockAvailableConnection();
      } else {
        logger.trace("created new active connection: {}", pc);
      }
    }

    if (pc != null) {
      activateAndValidateConnection(pc);
    } else {
      logger.error("Could not service check out request");
      throw new PoolExhaustedException("Pool is empty and connection creation failed");
    }

    return createConnectionProxy(pc);
  }
}
