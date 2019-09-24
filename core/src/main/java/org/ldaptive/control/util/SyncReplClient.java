/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.control.SyncRequestControl;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.SyncInfoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the sync repl control.
 *
 * @author  Middleware Services
 */
public class SyncReplClient
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection factory to get a connection from. */
  private final ConnectionFactory factory;

  /** Controls which mode the sync repl control should use. */
  private final boolean refreshAndPersist;

  /** Search operation handle. */
  private SearchOperationHandle handle;


  /**
   * Creates a new sync repl client.
   *
   * @param  cf  to get a connection from
   * @param  persist  whether to refresh and persist or just refresh
   */
  public SyncReplClient(final ConnectionFactory cf, final boolean persist)
  {
    factory = cf;
    refreshAndPersist = persist;
  }


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  /**
   * Invokes {@link #execute(SearchRequest, CookieManager, int)} with a {@link DefaultCookieManager} and a capacity of
   * {@link Integer#MAX_VALUE}.
   *
   * @param  request  search request to execute
   *
   * @return  blocking queue to wait for sync repl items
   *
   * @throws  LdapException  if the search fails
   */
  public BlockingQueue<SyncReplItem> execute(final SearchRequest request)
    throws LdapException
  {
    return execute(request, new DefaultCookieManager(), Integer.MAX_VALUE);
  }


  /**
   * Invokes {@link #execute(SearchRequest, CookieManager, int)} with a capacity of {@link Integer#MAX_VALUE}.
   *
   * @param  request  search request to execute
   * @param  manager  for reading and writing cookies
   *
   * @return  blocking queue to wait for sync repl items
   *
   * @throws  LdapException  if the search fails
   */
  public BlockingQueue<SyncReplItem> execute(final SearchRequest request, final CookieManager manager)
    throws LdapException
  {
    return execute(request, manager, Integer.MAX_VALUE);
  }


  /**
   * Performs an async search operation with the {@link SyncRequestControl}. The supplied request is modified in the
   * following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     SyncRequestControl}</li>
   * </ul>
   *
   * <p>The search request object should not be reused for any other search operations.</p>
   *
   * @param  request  search request to execute
   * @param  manager  for reading and writing cookies
   * @param  capacity  of the returned blocking queue
   *
   * @return  blocking queue to wait for sync repl items
   *
   * @throws  LdapException  if the search fails
   */
  public BlockingQueue<SyncReplItem> execute(
    final SearchRequest request,
    final CookieManager manager,
    final int capacity)
    throws LdapException
  {
    final BlockingQueue<SyncReplItem> queue = new LinkedBlockingQueue<>(capacity);

    request.setControls(
      new SyncRequestControl(
        refreshAndPersist ? SyncRequestControl.Mode.REFRESH_AND_PERSIST : SyncRequestControl.Mode.REFRESH_ONLY,
        manager.readCookie(),
        true));

    final SearchOperation search = new SearchOperation(factory, request);
    search.setResultHandlers(result -> {
      logger.debug("received {}", result);
      final SyncReplItem item = new SyncReplItem(new SyncReplItem.Result(result));
      if (item.getResult().getSyncDoneControl() != null) {
        final byte[] cookie = item.getResult().getSyncDoneControl().getCookie();
        if (cookie != null) {
          manager.writeCookie(cookie);
        }
      }
      try {
        queue.put(item);
      } catch (InterruptedException e) {
        logger.warn("Unable to enqueue result {}", result);
      }
    });
    search.setExceptionHandler(e -> {
      logger.debug("received exception", e);
      try {
        queue.put(new SyncReplItem(e));
      } catch (InterruptedException ex) {
        logger.warn("Unable to enqueue exception", ex);
      }
    });
    search.setEntryHandlers(entry -> {
      logger.debug("received {}", entry);
      final SyncReplItem item = new SyncReplItem(new SyncReplItem.Entry(entry));
      if (item.getEntry().getSyncStateControl() != null) {
        final byte[] cookie = item.getEntry().getSyncStateControl().getCookie();
        if (cookie != null) {
          manager.writeCookie(cookie);
        }
      }
      try {
        queue.put(item);
      } catch (InterruptedException e) {
        logger.warn("Unable to enqueue entry {}", entry);
      }
      return null;
    });
    search.setIntermediateResponseHandlers(response -> {
      if (SyncInfoMessage.OID.equals(response.getResponseName())) {
        logger.debug("received {}", response);
        final SyncInfoMessage message = (SyncInfoMessage) response;
        if (message.getCookie() != null) {
          manager.writeCookie(message.getCookie());
        }
        try {
          queue.put(new SyncReplItem(message));
        } catch (InterruptedException e) {
          logger.warn("Unable to enqueue intermediate response {}", response);
        }
      }
    });

    handle = search.send();
    return queue;
  }


  /**
   * Invokes a cancel operation on the underlying search operation.
   *
   * @return  cancel operation result
   *
   * @throws  LdapException  if the cancel operation fails
   */
  public ExtendedResponse cancel()
    throws LdapException
  {
    return handle.cancel().execute();
  }


  @Override
  public String toString()
  {
    return new StringBuilder().append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("factory=").append(factory).append(", ")
      .append("refreshAndPersist=").append(refreshAndPersist).append(", ")
      .append("handle=").append(handle).toString();
  }
}
