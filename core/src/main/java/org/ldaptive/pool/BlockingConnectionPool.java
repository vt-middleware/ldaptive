/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.ldaptive.Connection;
import org.ldaptive.DefaultConnectionFactory;

/**
 * Implements a pool of connections that has a set minimum and maximum size. The pool will not grow beyond the maximum
 * size and when the pool is exhausted, requests for new connections will block. The length of time the pool will block
 * is determined by {@link #getBlockWaitTime()}. By default the pool will block for 1 minute and there is no guarantee
 * that waiting threads will be serviced in the order in which they made their request. This implementation should be
 * used when you need to control the <em>exact</em> number of connections that can be created. See {@link
 * AbstractConnectionPool}.
 *
 * @author  Middleware Services
 */
public class BlockingConnectionPool extends AbstractConnectionPool
{

  /** Duration to wait for an available connection. */
  private Duration blockWaitTime = Duration.ofMinutes(1);


  /** Creates a new blocking pool. */
  public BlockingConnectionPool() {}


  /**
   * Creates a new blocking pool. The pool config is initialized with the default values.
   *
   * @param  cf  connection factory
   */
  public BlockingConnectionPool(final DefaultConnectionFactory cf)
  {
    this(new PoolConfig(), cf);
  }


  /**
   * Creates a new blocking pool.
   *
   * @param  pc  pool configuration
   * @param  cf  connection factory
   */
  public BlockingConnectionPool(final PoolConfig pc, final DefaultConnectionFactory cf)
  {
    setPoolConfig(pc);
    setDefaultConnectionFactory(cf);
  }


  /**
   * Returns the block wait time. Default time is 1 minute.
   *
   * @return  time to wait for available connections
   */
  public Duration getBlockWaitTime()
  {
    return blockWaitTime;
  }


  /**
   * Sets the block wait time. Default time is 1 minute.
   *
   * @param  time  to wait for available connections
   */
  public void setBlockWaitTime(final Duration time)
  {
    if (time != null && time.isNegative()) {
      throw new IllegalArgumentException("Block wait time cannot be negative");
    }
    blockWaitTime = time;
  }


  @Override
  public Connection getConnection()
    throws PoolException
  {
    throwIfNotInitialized();

    PooledConnectionProxy pc = null;
    boolean create = false;
    logger.trace("waiting on pool lock for check out {}", poolLock.getQueueLength());
    poolLock.lock();
    try {
      // if an available connection exists, use it
      // if no available connections and the pool can grow, attempt to create
      // otherwise the pool is full, block until a connection is returned
      if (!available.isEmpty()) {
        try {
          logger.trace("retrieve available connection from pool of size {}", available.size());
          pc = retrieveAvailableConnection();
        } catch (NoSuchElementException e) {
          logger.error("could not remove connection from list", e);
          throw new IllegalStateException("Pool is empty", e);
        }
      } else if (active.size() < getPoolConfig().getMaxPoolSize()) {
        logger.trace("pool can grow, attempt to create active connection in pool of " +
          "size {}", active.size());
        create = true;
      } else {
        logger.trace("pool is full, block until connection is available");
        pc = blockAvailableConnection();
      }
    } finally {
      poolLock.unlock();
    }

    if (create) {
      // previous block determined a creation should occur
      // block here until create occurs without locking the whole pool
      // if the pool is already maxed or creates are failing,
      // block until a connection is available
      checkOutLock.lock();
      try {
        boolean b = true;
        poolLock.lock();
        try {
          logger.trace("create connection in pool of size {}", available.size() + active.size());
          if (available.size() + active.size() == getPoolConfig().getMaxPoolSize()) {
            logger.trace("pool at maximum size, create not allowed");
            b = false;
          }
        } finally {
          poolLock.unlock();
        }
        if (b) {
          pc = createActiveConnection();
        }
      } finally {
        checkOutLock.unlock();
      }
      if (pc == null) {
        if (available.isEmpty() && active.isEmpty()) {
          logger.error("Could not service check out request");
          throw new PoolExhaustedException("Pool is empty and connection creation failed");
        }
        logger.debug("create failed, block until connection is available");
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


  /**
   * Attempts to retrieve a connection from the available queue.
   *
   * @return  connection from the pool
   *
   * @throws  NoSuchElementException  if the available queue is empty
   */
  protected PooledConnectionProxy retrieveAvailableConnection()
  {
    final PooledConnectionProxy pc;
    logger.trace("waiting on pool lock for retrieve available {}", poolLock.getQueueLength());
    poolLock.lock();
    try {
      pc = available.remove();
      active.add(pc);
      pc.getPooledConnectionStatistics().addActiveStat();
      logger.trace("retrieved available connection: {}", pc);
    } finally {
      poolLock.unlock();
    }
    return pc;
  }


  /**
   * This blocks until a connection can be acquired.
   *
   * @return  connection from the pool
   *
   * @throws  PoolException  if this method fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block time and it occurs
   * @throws  PoolInterruptedException  if the current thread is interrupted
   */
  protected PooledConnectionProxy blockAvailableConnection()
    throws PoolException
  {
    PooledConnectionProxy pc = null;
    logger.trace("waiting on pool lock for block available {}", poolLock.getQueueLength());
    poolLock.lock();
    try {
      while (pc == null) {
        logger.trace("available pool is empty, waiting...");
        if (blockWaitTime != null) {
          if (!poolNotEmpty.await(blockWaitTime.toMillis(), TimeUnit.MILLISECONDS)) {
            logger.debug("block time exceeded, throwing exception");
            throw new BlockingTimeoutException("Block time exceeded");
          }
        } else {
          poolNotEmpty.await();
        }
        logger.trace("notified to continue...");
        try {
          pc = retrieveAvailableConnection();
        } catch (NoSuchElementException e) {
          logger.trace("notified to continue but pool was empty");
        }
      }
    } catch (InterruptedException e) {
      logger.error("waiting for available connection interrupted", e);
      throw new PoolInterruptedException("Interrupted while waiting for an available connection", e);
    } finally {
      poolLock.unlock();
    }
    return pc;
  }


  @Override
  public void putConnection(final Connection c)
  {
    throwIfNotInitialized();

    final PooledConnectionProxy pc = retrieveConnectionProxy(c);
    final boolean valid = validateAndPassivateConnection(pc);
    logger.trace("waiting on pool lock for check in {}", poolLock.getQueueLength());
    poolLock.lock();
    try {
      if (!valid) {
        removeAvailableAndActiveConnection(pc);
      } else if (active.remove(pc)) {
        available.add(pc);
        pc.getPooledConnectionStatistics().addAvailableStat();
        logger.trace("returned active connection: {}", pc);
        poolNotEmpty.signal();
      } else if (available.contains(pc)) {
        logger.warn("returned available connection: {}", pc);
      } else {
        logger.warn("attempt to return unknown connection: {}", pc);
      }
    } finally {
      poolLock.unlock();
    }
  }
}
