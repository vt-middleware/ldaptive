/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.ad.control.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.ad.control.NotificationControl;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.ObjectSidHandler;
import org.ldaptive.async.AbandonOperation;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.async.AsyncSearchOperation;
import org.ldaptive.async.handler.AsyncRequestHandler;
import org.ldaptive.async.handler.ExceptionHandler;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.handler.OperationResponseHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the notification control.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class NotificationClient
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection to invoke the search operation on. */
  private final Connection connection;


  /**
   * Creates a new notification client.
   *
   * @param  conn  to execute the search operation on
   */
  public NotificationClient(final Connection conn)
  {
    connection = conn;
  }


  /**
   * Invokes {@link #execute(SearchRequest, int)} with a capacity of {@link
   * Integer#MAX_VALUE}.
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
   * Performs a search operation with the {@link NotificationControl}. The
   * supplied request is modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls(
   *     org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     NotificationControl}</li>
   *   <li>{@link SearchRequest#setSearchEntryHandlers(SearchEntryHandler...)}
   *     is invoked with a custom handler that places notification data in a
   *     blocking queue. The {@link ObjectGuidHandler} and {@link
   *     ObjectSidHandler} handlers are included as well.</li>
   *   <li>{@link AsyncSearchOperation#setExceptionHandler(ExceptionHandler)} is
   *     invoked with a custom handler that places the exception in a blocking
   *     queue.</li>
   * </ul>
   *
   * <p>The search request object should not be reused for any other search
   * operations.</p>
   *
   * @param  request  search request to execute
   * @param  capacity  of the returned blocking queue
   *
   * @return  blocking queue to wait for search entries
   *
   * @throws  LdapException  if the search fails
   */
  @SuppressWarnings("unchecked")
  public BlockingQueue<NotificationItem> execute(
    final SearchRequest request,
    final int capacity)
    throws LdapException
  {
    final BlockingQueue<NotificationItem> queue =
      new LinkedBlockingQueue<NotificationItem>(capacity);

    final AsyncSearchOperation search = new AsyncSearchOperation(connection);
    search.setOperationResponseHandlers(
      new OperationResponseHandler<SearchRequest, SearchResult>() {
        @Override
        public HandlerResult<Response<SearchResult>> handle(
          final Connection conn,
          final SearchRequest request,
          final Response<SearchResult> response)
          throws LdapException
        {
          try {
            logger.debug("received {}", response);
            search.shutdown();

            queue.put(new NotificationItem(response));
          } catch (Exception e) {
            logger.warn("Unable to enqueue response {}", response);
          }
          return new HandlerResult<Response<SearchResult>>(response);
        }
      });
    search.setAsyncRequestHandlers(
      new AsyncRequestHandler() {
        @Override
        public HandlerResult<AsyncRequest> handle(
          final Connection conn,
          final Request request,
          final AsyncRequest asyncRequest)
          throws LdapException
        {
          try {
            logger.debug("received {}", asyncRequest);
            queue.put(new NotificationItem(asyncRequest));
          } catch (Exception e) {
            logger.warn("Unable to enqueue async request {}", asyncRequest);
          }
          return new HandlerResult<AsyncRequest>(null);
        }
      });
    search.setExceptionHandler(
      new ExceptionHandler() {
        @Override
        public HandlerResult<Exception> handle(
          final Connection conn,
          final Request request,
          final Exception exception)
        {
          try {
            logger.debug("received exception:", exception);
            search.shutdown();
            queue.put(new NotificationItem(exception));
          } catch (Exception e) {
            logger.warn("Unable to enqueue exception:", exception);
          }
          return new HandlerResult<Exception>(null);
        }
      });

    request.setControls(new NotificationControl());
    request.setSearchEntryHandlers(
      new ObjectGuidHandler(),
      new ObjectSidHandler(),
      new SearchEntryHandler() {
        @Override
        public HandlerResult<SearchEntry> handle(
          final Connection conn,
          final SearchRequest request,
          final SearchEntry entry)
          throws LdapException
        {
          try {
            logger.debug("received {}", entry);
            queue.put(new NotificationItem(entry));
          } catch (Exception e) {
            logger.warn("Unable to enqueue entry {}", entry);
          }
          return new HandlerResult<SearchEntry>(null);
        }

        @Override
        public void initializeRequest(final SearchRequest request) {}
      });

    search.execute(request);
    return queue;
  }


  /**
   * Invokes an abandon operation on the supplied ldap message id. Convenience
   * method supplied to abandon async search operations.
   *
   * @param  messageId  of the operation to abandon
   *
   * @throws  LdapException  if the abandon operation fails
   */
  public void abandon(final int messageId)
    throws LdapException
  {
    final AbandonOperation abandon = new AbandonOperation(connection);
    abandon.execute(messageId);
  }


  /** Contains data returned when using the notification control. */
  public static class NotificationItem
  {

    /** Async request from the search operation. */
    private final AsyncRequest asyncRequest;

    /** Entry contained in this notification item. */
    private final SearchEntry searchEntry;

    /** Response contained in this notification item. */
    private final Response searchResponse;

    /** Exception thrown by the search operation. */
    private final Exception searchException;


    /**
     * Creates a new notification item.
     *
     * @param  request  that represents this item
     */
    public NotificationItem(final AsyncRequest request)
    {
      asyncRequest = request;
      searchEntry = null;
      searchResponse = null;
      searchException = null;
    }


    /**
     * Creates a new notification item.
     *
     * @param  entry  that represents this item
     */
    public NotificationItem(final SearchEntry entry)
    {
      asyncRequest = null;
      searchEntry = entry;
      searchResponse = null;
      searchException = null;
    }


    /**
     * Creates a new notification item.
     *
     * @param  response  that represents this item
     */
    public NotificationItem(final Response response)
    {
      asyncRequest = null;
      searchEntry = null;
      searchResponse = response;
      searchException = null;
    }


    /**
     * Creates a new notification item.
     *
     * @param  exception  that represents this item
     */
    public NotificationItem(final Exception exception)
    {
      asyncRequest = null;
      searchEntry = null;
      searchResponse = null;
      searchException = exception;
    }


    /**
     * Returns whether this item represents an async request.
     *
     * @return  whether this item represents an async request
     */
    public boolean isAsyncRequest()
    {
      return asyncRequest != null;
    }


    /**
     * Returns the async request contained in this item or null if this item
     * does not contain an async request.
     *
     * @return  async request
     */
    public AsyncRequest getAsyncRequest()
    {
      return asyncRequest;
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
     * Returns whether this item represents a search entry.
     *
     * @return  whether this item represents a search entry
     *
     * @deprecated  use {@link #isEntry()} instead
     */
    @Deprecated
    public boolean isSearchEntry()
    {
      return searchEntry != null;
    }


    /**
     * Returns the search entry contained in this item or null if this item does
     * not contain a search entry.
     *
     * @return  search entry
     */
    public SearchEntry getEntry()
    {
      return searchEntry;
    }


    /**
     * Returns whether this item represents a response.
     *
     * @return  whether this item represents a response
     */
    public boolean isResponse()
    {
      return searchResponse != null;
    }


    /**
     * Returns the response contained in this item or null if this item does not
     * contain a response.
     *
     * @return  response
     */
    public Response getResponse()
    {
      return searchResponse;
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
     * Returns the exception contained in this item or null if this item does
     * not contain an exception.
     *
     * @return  exception
     */
    public Exception getException()
    {
      return searchException;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      String s;
      if (isAsyncRequest()) {
        s = String.format(
          "[%s@%d::asyncRequest=%s]",
          getClass().getName(),
          hashCode(),
          asyncRequest);
      } else if (isEntry()) {
        s = String.format(
          "[%s@%d::searchEntry=%s]",
          getClass().getName(),
          hashCode(),
          searchEntry);
      } else if (isResponse()) {
        s = String.format(
          "[%s@%d::searchResponse=%s]",
          getClass().getName(),
          hashCode(),
          searchResponse);
      } else if (isException()) {
        s = String.format(
          "[%s@%d::searchException=%s]",
          getClass().getName(),
          hashCode(),
          searchException);
      } else {
        s = String.format("[%s@%d]", getClass().getName(), hashCode());
      }
      return s;
    }
  }
}
