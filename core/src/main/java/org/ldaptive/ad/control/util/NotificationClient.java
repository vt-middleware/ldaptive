/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Result;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.ad.control.NotificationControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the notification control.
 *
 * @author  Middleware Services
 */
public class NotificationClient
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection factory to get a connection from. */
  private final ConnectionFactory factory;

  /** Search operation handle. */
  private SearchOperationHandle handle;


  /**
   * Creates a new notification client.
   *
   * @param  cf  to get a connection from
   */
  public NotificationClient(final ConnectionFactory cf)
  {
    factory = LdapUtils.assertNotNullArg(cf, "Connection factory cannot be null");
  }


  /**
   * Invokes {@link #execute(SearchRequest, int)} with a capacity of {@link Integer#MAX_VALUE}.
   *
   * @param  request  search request to execute
   *
   * @return  blocking queue to wait for search entries
   *
   * @throws  LdapException  if the search fails
   */
  public BlockingQueue<NotificationItem> execute(final SearchRequest request)
    throws LdapException
  {
    return execute(request, Integer.MAX_VALUE);
  }


  /**
   * Performs a search operation with the {@link NotificationControl}. The supplied request is modified in the following
   * way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     NotificationControl}</li>
   * </ul>
   *
   * <p>The search request object should not be reused for any other search operations.</p>
   *
   * @param  request  search request to execute
   * @param  capacity  of the returned blocking queue
   *
   * @return  blocking queue to wait for search entries
   *
   * @throws  LdapException  if the search fails
   */
  public BlockingQueue<NotificationItem> execute(final SearchRequest request, final int capacity)
    throws LdapException
  {
    final BlockingQueue<NotificationItem> queue = new LinkedBlockingQueue<>(capacity);

    request.setControls(new NotificationControl());
    final SearchOperation search = new SearchOperation(factory, request);
    search.setResultHandlers(result -> {
      logger.debug("Received {}", result);
      try {
        queue.put(new NotificationItem(result));
      } catch (InterruptedException e) {
        logger.warn("Unable to enqueue result {}", result);
      }
    });
    search.setExceptionHandler(e -> {
      logger.debug("Received exception", e);
      try {
        queue.put(new NotificationItem(e));
      } catch (InterruptedException ex) {
        logger.warn("Unable to enqueue exception", ex);
      }
    });
    search.setEntryHandlers(entry -> {
      logger.debug("Received {}", entry);
      try {
        queue.put(new NotificationItem(entry));
      } catch (InterruptedException e) {
        logger.warn("Unable to enqueue entry {}", entry);
      }
      return entry;
    });

    handle = search.send();
    return queue;
  }


  /**
   * Invokes an abandon operation on the last invocation of {@link #execute(SearchRequest, int)}.
   */
  public void abandon()
  {
    handle.abandon();
  }


  /** Contains data returned when using the notification control. */
  public static class NotificationItem
  {

    /** Entry contained in this notification item. */
    private final LdapEntry searchEntry;

    /** Result contained in this notification item. */
    private final Result searchResult;

    /** Exception thrown by the search operation. */
    private final Exception searchException;


    /**
     * Creates a new notification item.
     *
     * @param  entry  that represents this item
     */
    public NotificationItem(final LdapEntry entry)
    {
      searchEntry = entry;
      searchResult = null;
      searchException = null;
    }


    /**
     * Creates a new notification item.
     *
     * @param  result  that represents this item
     */
    public NotificationItem(final Result result)
    {
      searchEntry = null;
      searchResult = result;
      searchException = null;
    }


    /**
     * Creates a new notification item.
     *
     * @param  exception  that represents this item
     */
    public NotificationItem(final Exception exception)
    {
      searchEntry = null;
      searchResult = null;
      searchException = exception;
    }


    /**
     * Returns whether this item represents a search entry.
     *
     * @return  whether this item represents a search entry
     */
    public boolean isEntry()
    {
      return searchEntry != null;
    }


    /**
     * Returns the search entry contained in this item or null if this item does not contain a search entry.
     *
     * @return  search entry
     */
    public LdapEntry getEntry()
    {
      return searchEntry;
    }


    /**
     * Returns whether this item represents a response.
     *
     * @return  whether this item represents a response
     */
    public boolean isResult()
    {
      return searchResult != null;
    }


    /**
     * Returns the response contained in this item or null if this item does not contain a response.
     *
     * @return  response
     */
    public Result getResult()
    {
      return searchResult;
    }


    /**
     * Returns whether this item represents an exception.
     *
     * @return  whether this item represents an exception
     */
    public boolean isException()
    {
      return searchException != null;
    }


    /**
     * Returns the exception contained in this item or null if this item does not contain an exception.
     *
     * @return  exception
     */
    public Exception getException()
    {
      return searchException;
    }


    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder("[").append(getClass().getName()).append("@").append(hashCode());
      if (isEntry()) {
        sb.append("::searchEntry=").append(searchEntry).append("]");
      } else if (isResult()) {
        sb.append("::searchResult=").append(searchResult).append("]");
      } else if (isException()) {
        sb.append("::syncReplException=").append(searchException).append("]");
      } else {
        sb.append("]");
      }
      return sb.toString();
    }
  }
}
