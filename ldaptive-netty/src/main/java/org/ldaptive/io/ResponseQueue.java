/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.ldaptive.protocol.UnsolicitedNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for operation handles that are waiting on a response from the LDAP server.
 *
 * @author  Middleware Services
 */
final class ResponseQueue
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(ResponseQueue.class);

  /** Time after which an operation handle should be removed from the queue. */
  private final Duration responseTTL;

  /** Map of message IDs to their operation handle. */
  private ConcurrentHashMap<Integer, OperationHandle> pending = new ConcurrentHashMap<>();

  /** Whether this queue is currently accepting new handles. */
  private boolean open;

  /** Executor for cleaning up expired requests. */
  private ScheduledExecutorService executor;


  /**
   * Creates a new response queue.
   *
   * @param  ttl  length of time that a handle can exist in the queue
   */
  ResponseQueue(final Duration ttl)
  {
    responseTTL = ttl;
  }


  /**
   * Open this queue to receive new handles.
   */
  public void open()
  {
    executor = Executors.newSingleThreadScheduledExecutor(
      r -> {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      });
    executor.scheduleAtFixedRate(
      () -> {
        logger.debug("begin cleanup task for {}", this);
        try {
          synchronized (pending) {
            final Instant now = Instant.now();
            final Iterator<OperationHandle> i = pending.values().iterator();
            while (i.hasNext()) {
              final OperationHandle h = i.next();
              if (h.getCreationTime().plus(responseTTL).isBefore(now)) {
                if (h.getSentTime() == null) {
                  h.exception(new LdapException("Operation aborted, request never sent"));
                  i.remove();
                } else {
                  h.abandon(new LdapException("Operation aborted, response never received"));
                }
              }
            }
          }
        } catch (Exception e) {
          logger.error("pending response cleanup failed", e);
        }
        logger.debug("end cleanup task for {}", this);
      },
      responseTTL.toMillis(),
      responseTTL.toMillis(),
      TimeUnit.MILLISECONDS);
    open = true;
  }


  /**
   * Close the queue to new handles.
   */
  public void close()
  {
    open = false;
    try {
      if (executor != null) {
        executor.shutdown();
      }
    } finally {
      executor = null;
    }
  }


  /**
   * Returns the operation handle for the supplied message id. Returns null if this queue is not open.
   *
   * @param  id  message id
   *
   * @return  operation handle or null
   */
  public OperationHandle get(final int id)
  {
    return open ? pending.get(id) : null;
  }


  /**
   * Removes the operation handle from the supplied message id. Returns null if this queue is not open.
   *
   * @param  id  message id
   *
   * @return  operation handle or null
   */
  public OperationHandle remove(final int id)
  {
    return open ? pending.remove(id) : null;
  }


  /**
   * Puts the supplied operation handle into the queue if the supplied id doesn't already exist in the queue.
   *
   * @param  id  message id
   * @param  handle  to put
   *
   * @return  null or existing operation handle for the id
   *
   * @throws  LdapException  if this queue is not open
   */
  public OperationHandle put(final int id, final OperationHandle handle)
    throws LdapException
  {
    if (!open) {
      throw new LdapException("Connection is closed, could not store handle " + handle);
    }
    return pending.putIfAbsent(id, handle);
  }


  /**
   * Returns all the operation handles in the queue.
   *
   * @return  all operation handles
   */
  public Collection<OperationHandle> handles()
  {
    return pending.values();
  }


  /**
   * Returns the size of this queue.
   *
   * @return  queue size
   */
  public int size()
  {
    return pending.size();
  }


  /**
   * Removes all operation handles from the queue.
   */
  public void clear()
  {
    pending.clear();
  }


  /**
   * Invokes {@link OperationHandle#abandon()} for all handles that have sent a request but not received a response.
   */
  public void abandonRequests()
  {
    synchronized (pending) {
      pending.values().stream().forEach(h -> {
        if (h.getSentTime() != null && h.getReceivedTime() == null) {
          h.abandon();
        }
      });
    }
  }


  /**
   * Notifies all operation handles in the queue that an exception has occurred. See {@link
   * OperationHandle#exception(Throwable)}. This method removes all handles from the queue.
   *
   * @param  e  exception to provides to handles
   */
  public void notifyOperationHandles(final Throwable e)
  {
    synchronized (pending) {
      final Iterator<OperationHandle> i = pending.values().iterator();
      while (i.hasNext()) {
        final OperationHandle h = i.next();
        i.remove();
        h.exception(e);
      }
    }
  }


  /**
   * Send the supplied notification to all handles waiting for a response.
   *
   * @param  notification  to send to response handles
   */
  public void notifyOperationHandles(final UnsolicitedNotification notification)
  {
    synchronized (pending) {
      pending.values().stream().forEach(h -> {
        if (h.getSentTime() != null && h.getReceivedTime() == null) {
          h.unsolicitedNotification(notification);
        }
      });
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("open=").append(open).append(", ")
      .append("responseTTL=").append(responseTTL).append(", ")
      .append("handles=").append(pending).toString();
  }


  @Override
  protected void finalize()
    throws Throwable
  {
    try {
      close();
    } finally {
      super.finalize();
    }
  }
}
