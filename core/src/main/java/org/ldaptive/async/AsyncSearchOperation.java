/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.async;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchReference;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.async.handler.ExceptionHandler;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.intermediate.IntermediateResponse;
import org.ldaptive.provider.SearchItem;
import org.ldaptive.provider.SearchListener;

/**
 * Executes an asynchronous ldap search operation.
 *
 * @author  Middleware Services
 */
public class AsyncSearchOperation
  extends AbstractAsyncOperation<SearchRequest, SearchResult>
{

  /** Cached thread executor to submit async operations to. */
  private final ExecutorService executorService =
    Executors.newCachedThreadPool();

  /** Whether the listener should spawn a new thread to process each result. */
  private boolean useMultiThreadedListener;


  /**
   * Creates a new async search operation.
   *
   * @param  conn  connection
   */
  public AsyncSearchOperation(final Connection conn)
  {
    super(conn);
  }


  /**
   * Returns whether the listener should spawn a new thread to process each
   * result.
   *
   * @return  whether the listener is multi-threaded
   */
  public boolean getUseMultiThreadedListener()
  {
    return useMultiThreadedListener;
  }


  /**
   * Sets whether the listener should spawn a new thread to process each result.
   *
   * @param  b  make the listener multi-threaded
   */
  public void setUseMultiThreadedListener(final boolean b)
  {
    useMultiThreadedListener = b;
  }


  /** {@inheritDoc} */
  @Override
  public FutureResponse<SearchResult> execute(final SearchRequest request)
    throws LdapException
  {
    final Future<Response<SearchResult>> future = executorService.submit(
      new Callable<Response<SearchResult>>() {
        @Override
        public Response<SearchResult> call()
          throws LdapException
        {
          final ExceptionHandler handler = getExceptionHandler();
          try {
            return AsyncSearchOperation.super.execute(request);
          } catch (LdapException | RuntimeException e) {
            if (handler != null) {
              handler.handle(getConnection(), request, e);
            }
            throw e;
          }
        }
      });
    return new FutureResponse<>(future);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<SearchResult> invoke(final SearchRequest request)
    throws LdapException
  {
    final AsyncSearchListener listener = new AsyncSearchListener(request);
    getConnection().getProviderConnection().searchAsync(request, listener);
    try {
      return listener.getResponse();
    } catch (InterruptedException e) {
      throw new LdapException("Asynchronous search interrupted", e);
    }
  }


  /**
   * Invokes {@link ExecutorService#shutdown()} on the underlying executor
   * service.
   */
  public void shutdown()
  {
    executorService.shutdown();
  }


  /** {@inheritDoc} */
  @Override
  protected void finalize()
    throws Throwable
  {
    try {
      shutdown();
    } finally {
      super.finalize();
    }
  }


  /**
   * Async search listener used to build a search result and invoke search
   * request handlers.
   */
  protected class AsyncSearchListener implements SearchListener
  {

    /** Containing request handlers. */
    private final SearchRequest searchRequest;

    /** To build as results arrive. */
    private final SearchResult searchResult;

    /** Wait for the response to arrive. */
    private final Semaphore responseLock = new Semaphore(0);

    /** To return when a response is received or the operation is aborted. */
    private Response<SearchResult> searchResponse;

    /** Thrown by the async search operation. */
    private LdapException searchException;


    /**
     * Creates a new async search listener.
     *
     * @param  request  ldap search request
     */
    public AsyncSearchListener(final SearchRequest request)
    {
      searchRequest = request;
      searchResult = new SearchResult(searchRequest.getSortBehavior());
    }


    /** {@inheritDoc} */
    @Override
    public void asyncRequestReceived(final AsyncRequest request)
    {
      logger.trace("received async request={}", request);
      if (useMultiThreadedListener) {
        executorService.submit(
          new Callable<Void>() {
            @Override
            public Void call()
              throws LdapException
            {
              try {
                processAsyncRequest(request);
              } catch (LdapException e) {
                logger.warn("Handler exception ignored", e);
              }
              return null;
            }
          });
      } else {
        try {
          processAsyncRequest(request);
        } catch (LdapException e) {
          logger.warn("Handler exception ignored", e);
        }
      }
    }


    /** {@inheritDoc} */
    @Override
    public void searchItemReceived(final SearchItem item)
    {
      logger.trace("received search item={}", item);
      if (useMultiThreadedListener) {
        executorService.submit(
          new Callable<Void>() {
            @Override
            public Void call()
              throws LdapException
            {
              try {
                processSearchItem(item);
              } catch (LdapException e) {
                logger.warn("Handler exception ignored", e);
              }
              return null;
            }
          });
      } else {
        try {
          processSearchItem(item);
        } catch (LdapException e) {
          logger.warn("Handler exception ignored", e);
        }
      }
    }


    /** {@inheritDoc} */
    @Override
    public void responseReceived(final Response<Void> response)
    {
      searchResponse = new Response<>(
        searchResult,
        response.getResultCode(),
        response.getMessage(),
        response.getMatchedDn(),
        response.getControls(),
        response.getReferralURLs(),
        response.getMessageId());
      responseLock.release();
    }


    /**
     * Returns the response data associated with this search, blocking until a
     * response is available.
     *
     * @return  response data
     *
     * @throws  InterruptedException  if this thread is interrupted before a
     * response is received
     * @throws  LdapException  if the async search encountered an error
     */
    public Response<SearchResult> getResponse()
      throws InterruptedException, LdapException
    {
      responseLock.acquire();
      if (searchException != null) {
        throw searchException;
      }
      return searchResponse;
    }


    /** {@inheritDoc} */
    @Override
    public void exceptionReceived(final Exception exception)
    {
      logger.trace("received exception={}", exception);
      if (exception instanceof LdapException) {
        searchException = (LdapException) exception;
      } else {
        searchException = new LdapException(exception);
      }
      responseLock.release();
    }


    /**
     * Invokes the handlers for the supplied async request. Calls {@link
     * #responseReceived(Response)} if a handler aborts the operation.
     *
     * @param  request  to handle
     *
     * @throws  LdapException  if a handler throws
     */
    protected void processAsyncRequest(final AsyncRequest request)
      throws LdapException
    {
      logger.trace("processing async request={}", request);

      final HandlerResult<AsyncRequest> hr = executeHandlers(
        getAsyncRequestHandlers(),
        searchRequest,
        request);
      if (hr.getAbort()) {
        logger.debug("Aborting search on async request=%s", request);
        responseReceived(new Response<Void>(null, null));
      }
    }


    /**
     * Invokes the handlers for the supplied search item. Calls {@link
     * #responseReceived(Response)} if a handler aborts the operation.
     *
     * @param  item  to handle
     *
     * @throws  LdapException  if a handler throws
     */
    protected void processSearchItem(final SearchItem item)
      throws LdapException
    {
      logger.trace("processing search item={}", item);
      if (item.isSearchEntry()) {
        final SearchEntry se = item.getSearchEntry();
        if (se != null) {
          final HandlerResult<SearchEntry> hr = executeHandlers(
            searchRequest.getSearchEntryHandlers(),
            searchRequest,
            se);
          if (hr.getResult() != null) {
            searchResult.addEntry(hr.getResult());
          }
          if (hr.getAbort()) {
            logger.debug("Aborting search on entry=%s", se);
            responseReceived(new Response<Void>(null, null));
          }
        }
      } else if (item.isSearchReference()) {
        final SearchReference sr = item.getSearchReference();
        if (sr != null) {
          final HandlerResult<SearchReference> hr = executeHandlers(
            searchRequest.getSearchReferenceHandlers(),
            searchRequest,
            sr);
          if (hr.getResult() != null) {
            searchResult.addReference(hr.getResult());
          }
          if (hr.getAbort()) {
            logger.debug("Aborting search on reference=%s", sr);
            responseReceived(new Response<Void>(null, null));
          }
        }
      } else if (item.isIntermediateResponse()) {
        final IntermediateResponse ir = item.getIntermediateResponse();
        if (ir != null) {
          final HandlerResult<IntermediateResponse> hr = executeHandlers(
            searchRequest.getIntermediateResponseHandlers(),
            searchRequest,
            ir);
          if (hr.getAbort()) {
            logger.debug("Aborting search on intermediate response=%s", ir);
            responseReceived(new Response<Void>(null, null));
          }
        }
      }
    }
  }
}
