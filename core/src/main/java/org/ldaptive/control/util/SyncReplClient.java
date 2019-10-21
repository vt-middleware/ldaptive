/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.function.Consumer;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.control.SyncDoneControl;
import org.ldaptive.control.SyncRequestControl;
import org.ldaptive.control.SyncStateControl;
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

  /** Invoked when an entry is received. */
  private Consumer<LdapEntry> onEntry;

  /** Invoked when a result is received. */
  private Consumer<Result> onResult;

  /** Invoked when a sync info message is received. */
  private Consumer<SyncInfoMessage> onMessage;

  /** Invoked when an exception is received. */
  private Consumer<Exception> onException;

  /** Whether the sync repl search has received a result response. */
  private boolean receivedResult;


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
   * Sets the onEntry consumer.
   *
   * @param  consumer  to invoke when an entry is received
   */
  public void setOnEntry(final Consumer<LdapEntry> consumer)
  {
    onEntry = consumer;
  }


  /**
   * Sets the onResult consumer.
   *
   * @param  consumer  to invoke when a result is received
   */
  public void setOnResult(final Consumer<Result> consumer)
  {
    onResult = consumer;
  }


  /**
   * Sets the onMessage consumer.
   *
   * @param  consumer  to invoke when a sync info message is received
   */
  public void setOnMessage(final Consumer<SyncInfoMessage> consumer)
  {
    onMessage = consumer;
  }


  /**
   * Sets the onException consumer.
   *
   * @param  consumer  to invoke when a sync info message is received
   */
  public void setOnException(final Consumer<Exception> consumer)
  {
    onException = consumer;
  }


  /**
   * Invokes {@link #send(SearchRequest, CookieManager)} with a {@link DefaultCookieManager}.
   *
   * @param  request  search request to execute
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the search fails
   */
  public SearchOperationHandle send(final SearchRequest request)
    throws LdapException
  {
    return send(request, new DefaultCookieManager());
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
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the search fails
   */
  public SearchOperationHandle send(final SearchRequest request, final CookieManager manager)
    throws LdapException
  {
    request.setControls(
      new SyncRequestControl(
        refreshAndPersist ? SyncRequestControl.Mode.REFRESH_AND_PERSIST : SyncRequestControl.Mode.REFRESH_ONLY,
        manager.readCookie(),
        true));

    final SearchOperation search = new SearchOperation(factory, request);
    search.setResultHandlers(result -> {
      logger.debug("received {}", result);
      receivedResult = true;
      if (result.getControl(SyncDoneControl.OID) != null) {
        final SyncDoneControl syncDoneControl = (SyncDoneControl) result.getControl(SyncDoneControl.OID);
        final byte[] cookie = syncDoneControl.getCookie();
        if (cookie != null) {
          try {
            manager.writeCookie(cookie);
          } catch (Exception e) {
            logger.warn("Unable to write cookie", e);
          }
        }
      }
      try {
        onResult.accept(result);
      } catch (Exception e) {
        logger.warn("Unable to process result {}", result);
        try {
          onException.accept(e);
        } catch (Exception ex) {
          logger.warn("Unable to process result exception", ex);
        }
      }
    });
    search.setExceptionHandler(e -> {
      logger.debug("received exception", e);
      try {
        onException.accept(e);
      } catch (Exception ex) {
        logger.warn("Unable to process exception", ex);
      }
    });
    search.setEntryHandlers(entry -> {
      logger.debug("received {}", entry);
      if (entry.getControl(SyncStateControl.OID) != null) {
        final SyncStateControl syncStateControl = (SyncStateControl) entry.getControl(SyncStateControl.OID);
        final byte[] cookie = syncStateControl.getCookie();
        if (cookie != null) {
          try {
            manager.writeCookie(cookie);
          } catch (Exception e) {
            logger.warn("Unable to write cookie", e);
          }
        }
      }
      try {
        onEntry.accept(entry);
      } catch (Exception e) {
        logger.warn("Unable to process entry {}", entry);
        try {
          onException.accept(e);
        } catch (Exception ex) {
          logger.warn("Unable to process entry exception", ex);
        }
      }
      return null;
    });
    search.setIntermediateResponseHandlers(response -> {
      if (SyncInfoMessage.OID.equals(response.getResponseName())) {
        logger.debug("received {}", response);
        final SyncInfoMessage message = (SyncInfoMessage) response;
        if (message.getCookie() != null) {
          try {
            manager.writeCookie(message.getCookie());
          } catch (Exception e) {
            logger.warn("Unable to write cookie", e);
          }
        }
        try {
          onMessage.accept(message);
        } catch (Exception e) {
          logger.warn("Unable to process intermediate response {}", response);
          try {
            onException.accept(e);
          } catch (Exception ex) {
            logger.warn("Unable to process intermediate response exception", ex);
          }
        }
      }
    });

    receivedResult = false;
    handle = search.send();
    return handle;
  }


  /**
   * Returns whether a search result has been received by this client.
   *
   * @return  whether a search result has been received
   */
  public boolean isComplete()
  {
    return receivedResult;
  }


  /**
   * Invokes a cancel operation on the underlying search operation and shuts down the executor.
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


  /**
   * Closes the connection factory.
   */
  public void close()
  {
    factory.close();
  }


  @Override
  public String toString()
  {
    return new StringBuilder().append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("factory=").append(factory).append(", ")
      .append("refreshAndPersist=").append(refreshAndPersist).append(", ")
      .append("onEntry=").append(onEntry).append(", ")
      .append("onResult=").append(onResult).append(", ")
      .append("onMessage=").append(onMessage).append(", ")
      .append("onException=").append(onException).append(", ")
      .append("handle=").append(handle).toString();
  }
}
