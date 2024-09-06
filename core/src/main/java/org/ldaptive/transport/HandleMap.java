/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.extended.UnsolicitedNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for operation handles that are waiting on a response from the LDAP server.
 *
 * @author  Middleware Services
 */
public final class HandleMap
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(HandleMap.class);

  /** Throttle requests system property. */
  private static final String THROTTLE_REQUESTS_PROPERTY = "org.ldaptive.transport.throttleRequests";

  /** Throttle timeout system property. */
  private static final String THROTTLE_TIMEOUT_PROPERTY = "org.ldaptive.transport.throttleTimeout";

  /** If property is greater than zero, use the throttle semaphore. */
  private static final int THROTTLE_REQUESTS = Integer.parseInt(System.getProperty(THROTTLE_REQUESTS_PROPERTY, "0"));

  /** Maximum time to wait for the throttle semaphore. Default is 60 seconds. */
  private static final Duration THROTTLE_TIMEOUT = Duration.ofSeconds(
    Long.parseLong(System.getProperty(THROTTLE_TIMEOUT_PROPERTY, "60")));

  /** Map of message IDs to their operation handle. */
  private final Map<Integer, DefaultOperationHandle<?, ?>> pending = new ConcurrentHashMap<>();

  /** Only one notification can occur at a time. */
  private final AtomicBoolean notificationLock = new AtomicBoolean();

  /** Semaphore to throttle incoming requests. */
  private final Semaphore throttle;

  /** Whether this queue is currently accepting new handles. */
  private boolean open;


  /**
   * Creates a new handle map.
   */
  public HandleMap()
  {
    if (THROTTLE_REQUESTS > 0) {
      throttle = new Semaphore(THROTTLE_REQUESTS);
    } else {
      throttle = null;
    }
  }


  /**
   * Open this queue to receive new handles.
   */
  public void open()
  {
    open = true;
  }


  /**
   * Close the queue to new handles.
   */
  public void close()
  {
    open = false;
  }


  /**
   * Returns whether this handle map is open.
   *
   * @return  is open
   */
  public boolean isOpen()
  {
    return open;
  }


  /**
   * Returns the operation handle for the supplied message id. Returns null if this queue is not open.
   *
   * @param  id  message id
   *
   * @return  operation handle or null
   */
  public DefaultOperationHandle<?, ?> get(final int id)
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
  public DefaultOperationHandle<?, ?> remove(final int id)
  {
    if (open) {
      final DefaultOperationHandle<?, ?> handle = pending.remove(id);
      releaseThrottle(1);
      return handle;
    }
    return null;
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
  public DefaultOperationHandle<?, ?> put(final int id, final DefaultOperationHandle<?, ?> handle)
    throws LdapException
  {
    if (!open) {
      throw new LdapException(ResultCode.CONNECT_ERROR, "Connection is closed, could not store handle " + handle);
    }
    acquireThrottle();
    return pending.putIfAbsent(id, handle);
  }


  /**
   * Returns all the operation handles in the queue.
   *
   * @return  all operation handles
   */
  public Collection<DefaultOperationHandle<?, ?>> handles()
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
    releaseThrottle(pending.size());
    pending.clear();
  }


  /**
   * Attempt to acquire the throttle semaphore. No-op if throttling is not enabled.
   *
   * @throws  LdapException  if the semaphore cannot be acquired or the thread is interrupted
   */
  private void acquireThrottle()
    throws LdapException
  {
    if (throttle != null) {
      try {
        if (!throttle.tryAcquire(THROTTLE_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
          throw new LdapException(ResultCode.LOCAL_ERROR, "Could not acquire request semaphore");
        }
      } catch (InterruptedException e) {
        throw new LdapException(ResultCode.LOCAL_ERROR, "Could not acquire request semaphore", e);
      }
    }
  }


  /**
   * Release permits on the throttle semaphore. No-op if throttling is not enabled.
   *
   * @param  permits  number of permits to release
   */
  private void releaseThrottle(final int permits)
  {
    if (throttle != null) {
      throttle.release(permits);
    }
  }


  /**
   * Invokes {@link DefaultOperationHandle#abandon()} for all handles that have sent a request but not received a
   * response. This method removes all handles from the queue.
   */
  public void abandonRequests()
  {
    if (notificationLock.compareAndSet(false, true)) {
      try {
        final Iterator<DefaultOperationHandle<?, ?>> i = pending.values().iterator();
        while (i.hasNext()) {
          final DefaultOperationHandle<?, ?> h = i.next();
          if (h.getSentTime() != null && h.getReceivedTime() == null) {
            i.remove();
            releaseThrottle(1);
            h.abandon();
          }
        }
      } finally {
        notificationLock.set(false);
      }
    } else {
      LOGGER.debug("Handle notification is already in progress");
    }
  }


  /**
   * Notifies all operation handles in the queue that an exception has occurred. See {@link
   * DefaultOperationHandle#exception(LdapException)}. This method removes all handles from the queue.
   *
   * @param  e  exception to provides to handles
   */
  public void notifyOperationHandles(final LdapException e)
  {
    if (notificationLock.compareAndSet(false, true)) {
      try {
        final Iterator<DefaultOperationHandle<?, ?>> i = pending.values().iterator();
        while (i.hasNext()) {
          final DefaultOperationHandle<?, ?> h = i.next();
          i.remove();
          releaseThrottle(1);
          h.exception(e);
        }
      } finally {
        notificationLock.set(false);
      }
    } else {
      LOGGER.debug("Handle notification is already in progress");
    }
  }


  /**
   * Send the supplied notification to all handles waiting for a response.
   *
   * @param  notification  to send to response handles
   */
  public void notifyOperationHandles(final UnsolicitedNotification notification)
  {
    if (notificationLock.compareAndSet(false, true)) {
      try {
        pending.values().forEach(h -> {
          if (h.getSentTime() != null && h.getReceivedTime() == null) {
            h.unsolicitedNotification(notification);
          }
        });
      } finally {
        notificationLock.set(false);
      }
    } else {
      LOGGER.debug("Handle notification is already in progress");
    }
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "open=" + open + ", " +
      "throttle=" + throttle + ", " +
      "handles=" + pending;
  }
}
