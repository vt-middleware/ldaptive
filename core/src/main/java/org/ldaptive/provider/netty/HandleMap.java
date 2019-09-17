/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.ldaptive.LdapException;
import org.ldaptive.extended.UnsolicitedNotification;
import org.ldaptive.provider.DefaultOperationHandle;

/**
 * Container for operation handles that are waiting on a response from the LDAP server.
 *
 * @author  Middleware Services
 */
final class HandleMap
{

  /** Map of message IDs to their operation handle. */
  private final ConcurrentHashMap<Integer, DefaultOperationHandle> pending = new ConcurrentHashMap<>();

  /** Whether this queue is currently accepting new handles. */
  private final AtomicBoolean open = new AtomicBoolean();


  /**
   * Creates a new handle map.
   */
  HandleMap() {}


  /**
   * Open this queue to receive new handles.
   */
  public void open()
  {
    open.set(true);
  }


  /**
   * Close the queue to new handles.
   */
  public void close()
  {
    open.set(false);
  }


  /**
   * Returns the operation handle for the supplied message id. Returns null if this queue is not open.
   *
   * @param  id  message id
   *
   * @return  operation handle or null
   */
  public DefaultOperationHandle get(final int id)
  {
    return open.get() ? pending.get(id) : null;
  }


  /**
   * Removes the operation handle from the supplied message id. Returns null if this queue is not open.
   *
   * @param  id  message id
   *
   * @return  operation handle or null
   */
  public DefaultOperationHandle remove(final int id)
  {
    return open.get() ? pending.remove(id) : null;
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
  public DefaultOperationHandle put(final int id, final DefaultOperationHandle handle)
    throws LdapException
  {
    if (!open.get()) {
      throw new LdapException("Connection is closed, could not store handle " + handle);
    }
    return pending.putIfAbsent(id, handle);
  }


  /**
   * Returns all the operation handles in the queue.
   *
   * @return  all operation handles
   */
  public Collection<DefaultOperationHandle> handles()
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
   * Invokes {@link DefaultOperationHandle#abandon()} for all handles that have sent a request but not received a
   * response.
   */
  public void abandonRequests()
  {
    synchronized (pending) {
      pending.values().forEach(h -> {
        if (h.getSentTime() != null && h.getReceivedTime() == null) {
          h.abandon();
        }
      });
    }
  }


  /**
   * Notifies all operation handles in the queue that an exception has occurred. See {@link
   * DefaultOperationHandle#exception(Throwable)}. This method removes all handles from the queue.
   *
   * @param  e  exception to provides to handles
   */
  public void notifyOperationHandles(final Throwable e)
  {
    synchronized (pending) {
      final Iterator<DefaultOperationHandle> i = pending.values().iterator();
      while (i.hasNext()) {
        final DefaultOperationHandle h = i.next();
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
      pending.values().forEach(h -> {
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
      .append("handles=").append(pending).toString();
  }
}
