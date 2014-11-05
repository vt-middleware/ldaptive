/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Statistics associated with a connection's activity in the pool. Exposes the
 * timestamps when this connection entered both the available pool and the
 * active pool. A size of 512 uses approximately 50 kilobytes of memory per
 * connection.
 *
 * @author  Middleware Services
 */
public class PooledConnectionStatistics
{

  /** Number of available and active timestamps to store. */
  private final int size;

  /** Available stats. */
  private final Deque<Long> availableStats;

  /** Active stats. */
  private final Deque<Long> activeStats;


  /**
   * Creates a new pooled connection statistics.
   *
   * @param  i  number of timestamps to store
   */
  public PooledConnectionStatistics(final int i)
  {
    size = i;
    availableStats = new LinkedList<Long>() {


      /** {@inheritDoc} */
      @Override
      public boolean add(final Long e)
      {
        if (size < 1) {
          return false;
        }

        final boolean b = super.add(e);
        while (size() > size) {
          remove();
        }
        return b;
      }
    };
    activeStats = new LinkedList<Long>() {


      /** {@inheritDoc} */
      @Override
      public boolean add(final Long e)
      {
        if (size < 1) {
          return false;
        }

        final boolean b = super.add(e);
        while (size() > size) {
          remove();
        }
        return b;
      }
    };
  }


  /**
   * Returns all the available timestamp statistics.
   *
   * @return  available timestamp statistics
   */
  public Deque<Long> getAvailableStats()
  {
    return availableStats;
  }


  /**
   * Returns the last timestamp at which this connection was made available.
   *
   * @return  millisecond timestamp
   */
  public Long getLastAvailableState()
  {
    return availableStats.peekLast();
  }


  /** Inserts the current timestamp into the available statistics. */
  public synchronized void addAvailableStat()
  {
    availableStats.add(System.currentTimeMillis());
  }


  /**
   * Returns all the active timestamp statistics.
   *
   * @return  active timestamp statistics
   */
  public Deque<Long> getActiveStats()
  {
    return activeStats;
  }


  /**
   * Returns the last timestamp at which this connection was made active.
   *
   * @return  millisecond timestamp
   */
  public Long getLastActiveStat()
  {
    return activeStats.peekLast();
  }


  /** Inserts the current timestamp into the active statistics. */
  public synchronized void addActiveStat()
  {
    activeStats.add(System.currentTimeMillis());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format("[%s@%d::size=%s]", getClass().getName(), hashCode(), size);
  }
}
