/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.async.AbandonOperation;
import org.ldaptive.async.AsyncSearchOperation;
import org.ldaptive.control.PersistentSearchChangeType;
import org.ldaptive.control.PersistentSearchRequestControl;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.handler.SearchEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the persistent search control.
 *
 * @author  Middleware Services
 */
public class PersistentSearchClient
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection to invoke the search operation on. */
  private final Connection connection;

  /** Change types. */
  private final EnumSet<PersistentSearchChangeType> changeTypes;

  /** Whether to return only changed entries. */
  private final boolean changesOnly;

  /** Whether to return an Entry Change Notification control. */
  private final boolean returnEcs;


  /**
   * Creates a new persistent search client.
   *
   * @param  conn  to execute the async search operation on
   * @param  types  persistent search change types
   * @param  co  whether only changed entries are returned
   * @param  re  return an Entry Change Notification control
   */
  public PersistentSearchClient(
    final Connection conn,
    final EnumSet<PersistentSearchChangeType> types,
    final boolean co,
    final boolean re)
  {
    connection = conn;
    changeTypes = types;
    changesOnly = co;
    returnEcs = re;
  }


  /**
   * Invokes {@link #execute(SearchRequest, int)} with a capacity of {@link Integer#MAX_VALUE}.
   *
   * @param  request  search request to execute
   *
   * @return  blocking queue to wait for persistent search items
   *
   * @throws  LdapException  if the search fails
   */
  public BlockingQueue<PersistentSearchItem> execute(final SearchRequest request)
    throws LdapException
  {
    return execute(request, Integer.MAX_VALUE);
  }


  /**
   * Performs an async search operation with the {@link PersistentSearchRequestControl}. The supplied request is
   * modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     PersistentSearchRequestControl}</li>
   *   <li>{@link SearchRequest#setSearchEntryHandlers(SearchEntryHandler...)} is invoked with a custom handler that
   *     places persistent search data in a blocking queue.</li>
   *   <li>{@link AsyncSearchOperation#setExceptionHandler(org.ldaptive.async.handler.ExceptionHandler)} is invoked with
   *     a custom handler that places the exception in a blocking queue.</li>
   * </ul>
   *
   * <p>The search request object should not be reused for any other search operations.</p>
   *
   * @param  request  search request to execute
   * @param  capacity  of the returned blocking queue
   *
   * @return  blocking queue to wait for persistent search items
   *
   * @throws  LdapException  if the search fails
   */
  @SuppressWarnings("unchecked")
  public BlockingQueue<PersistentSearchItem> execute(final SearchRequest request, final int capacity)
    throws LdapException
  {
    final BlockingQueue<PersistentSearchItem> queue = new LinkedBlockingQueue<>(capacity);

    final AsyncSearchOperation search = new AsyncSearchOperation(connection);
    search.setOperationResponseHandlers(
      (conn, req, res) -> {
        try {
          logger.debug("received {}", res);
          search.shutdown();

          queue.put(new PersistentSearchItem(res));
        } catch (Exception e) {
          logger.warn("Unable to enqueue response {}", res);
        }
        return new HandlerResult<>(res);
      });
    search.setAsyncRequestHandlers(
      (conn, req, asyncReq) -> {
        try {
          logger.debug("received {}", asyncReq);
          queue.put(new PersistentSearchItem(asyncReq));
        } catch (Exception e) {
          logger.warn("Unable to enqueue async request {}", asyncReq);
        }
        return new HandlerResult<>(null);
      });
    search.setExceptionHandler(
      (conn, req, exception) -> {
        try {
          logger.debug("received exception:", exception);
          search.shutdown();
          queue.put(new PersistentSearchItem(exception));
        } catch (Exception e) {
          logger.warn("Unable to enqueue exception:", exception);
        }
        return new HandlerResult<>(null);
      });

    request.setControls(new PersistentSearchRequestControl(changeTypes, changesOnly, returnEcs, true));
    request.setSearchEntryHandlers(
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

            final PersistentSearchItem item = new PersistentSearchItem(new PersistentSearchItem.Entry(entry));
            queue.put(item);
          } catch (Exception e) {
            logger.warn("Unable to enqueue entry {}", entry);
          }
          return new HandlerResult<>(null);
        }

        @Override
        public void initializeRequest(final SearchRequest request) {}
      });

    search.execute(request);
    return queue;
  }


  /**
   * Invokes an abandon operation on the supplied ldap message id. Convenience method supplied to abandon persistent
   * search operations.
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
}
