/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchReference;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.intermediate.IntermediateResponse;

/**
 * Executes a search filter and places the results of the operation on a blocking queue. The capacity of the queue can
 * be configured to address memory concerns related to large result sets.
 *
 * @author  Middleware Services
 */
public class QueueingSearchExecutor extends AbstractSearchExecutor
{

  /** Capacity of the blocking queue created by search operations. */
  private final int queueCapacity;


  /** Default constructor. */
  public QueueingSearchExecutor()
  {
    this(Integer.MAX_VALUE, Executors.newCachedThreadPool());
  }


  /**
   * Creates a new blocking search executor.
   *
   * @param  capacity  of the blocking queue returned from search operations
   */
  public QueueingSearchExecutor(final int capacity)
  {
    this(capacity, Executors.newCachedThreadPool());
  }


  /**
   * Creates a new blocking search executor.
   *
   * @param  es  executor service
   */
  public QueueingSearchExecutor(final ExecutorService es)
  {
    this(Integer.MAX_VALUE, es);
  }


  /**
   * Creates a new blocking search executor.
   *
   * @param  capacity  of the blocking queue returned from search operations
   * @param  es  executor service
   */
  public QueueingSearchExecutor(final int capacity, final ExecutorService es)
  {
    super(es);
    queueCapacity = capacity;
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   *
   * @return  blocking queue to receive search items
   *
   * @throws  LdapException  if the connection factory cannot create a connection
   */
  public BlockingQueue<SearchItem> search(final ConnectionFactory factory)
    throws LdapException
  {
    return search(factory, null, null, (org.ldaptive.handler.SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   *
   * @return  blocking queue to receive search items
   *
   * @throws  LdapException  if the connection factory cannot create a connection
   */
  public BlockingQueue<SearchItem> search(final ConnectionFactory factory, final String filter)
    throws LdapException
  {
    return search(factory, new SearchFilter(filter), null, (org.ldaptive.handler.SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   *
   * @return  blocking queue to receive search items
   *
   * @throws  LdapException  if the connection factory cannot create a connection
   */
  public BlockingQueue<SearchItem> search(final ConnectionFactory factory, final SearchFilter filter)
    throws LdapException
  {
    return search(factory, filter, null, (org.ldaptive.handler.SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   * @param  attrs  to return
   *
   * @return  blocking queue to receive search items
   *
   * @throws  LdapException  if the connection factory cannot create a connection
   */
  public BlockingQueue<SearchItem> search(final ConnectionFactory factory, final String filter, final String... attrs)
    throws LdapException
  {
    return search(factory, new SearchFilter(filter), attrs, (org.ldaptive.handler.SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   * @param  attrs  to return
   *
   * @return  blocking queue to receive search items
   *
   * @throws  LdapException  if the connection factory cannot create a connection
   */
  public BlockingQueue<SearchItem> search(
    final ConnectionFactory factory,
    final SearchFilter filter,
    final String... attrs)
    throws LdapException
  {
    return search(factory, filter, attrs, (org.ldaptive.handler.SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   * @param  attrs  to return
   * @param  handlers  entry handlers
   *
   * @return  blocking queue to receive search items
   *
   * @throws  LdapException  if the connection factory cannot create a connection
   */
  public BlockingQueue<SearchItem> search(
    final ConnectionFactory factory,
    final SearchFilter filter,
    final String[] attrs,
    final org.ldaptive.handler.SearchEntryHandler... handlers)
    throws LdapException
  {
    final BlockingQueue<SearchItem> queue = new LinkedBlockingQueue<>(queueCapacity);

    final SearchRequest sr = newSearchRequest(this);
    if (filter != null) {
      sr.setSearchFilter(filter);
    }
    if (attrs != null) {
      sr.setReturnAttributes(attrs);
    }
    if (handlers != null) {
      sr.setSearchEntryHandlers(handlers);
    }
    configureSearchRequest(sr, queue);

    final Connection conn = factory.getConnection();
    final SearchOperation op = createSearchOperation(conn);

    final ExecutorService service = getExecutorService();
    service.submit(createCallable(conn, op, sr, queue));

    return queue;
  }


  /**
   * Returns a {@link Callable} that executes the supplied request with the supplied operation in a try-finally block
   * that opens and closes the connection.
   *
   * @param  conn  connection that the operation will execute on
   * @param  operation  to execute
   * @param  request  to pass to the operation
   * @param  queue  to put search items on
   *
   * @return  callable for the supplied operation and request
   */
  protected Callable<Response<SearchResult>> createCallable(
    final Connection conn,
    final SearchOperation operation,
    final SearchRequest request,
    final BlockingQueue<SearchItem> queue)
  {
    return
      new Callable<Response<SearchResult>>() {
      @Override
      public Response<SearchResult> call()
        throws LdapException
      {
        try {
          conn.open();

          SearchItem item;
          try {
            final Response<SearchResult> response = operation.execute(request);
            item = new SearchItem(response);
          } catch (Exception e) {
            item = new SearchItem(e);
          }
          try {
            logger.debug("received {}", item);
            queue.put(item);
          } catch (InterruptedException e1) {
            logger.warn("Unable to insert item {}", item);
          }
          return null;
        } finally {
          conn.close();
        }
      }
    };
  }


  /**
   * Sets the appropriate handlers on the supplied search request so that entries, references, and intermediate
   * responses are made available on the supplied queue.
   *
   * @param  request  search request to modify
   * @param  queue  to use in the handlers
   */
  protected void configureSearchRequest(final SearchRequest request, final BlockingQueue<SearchItem> queue)
  {
    if (request.getSearchEntryHandlers() != null) {
      request.setSearchEntryHandlers(
        LdapUtils.concatArrays(
          request.getSearchEntryHandlers(),
          new org.ldaptive.handler.SearchEntryHandler[] {new SearchEntryHandler(queue), }));
    } else {
      request.setSearchEntryHandlers(new SearchEntryHandler(queue));
    }
    if (request.getSearchReferenceHandlers() != null) {
      request.setSearchReferenceHandlers(
        LdapUtils.concatArrays(
          request.getSearchReferenceHandlers(),
          new org.ldaptive.handler.SearchReferenceHandler[] {new SearchReferenceHandler(queue), }));
    } else {
      request.setSearchReferenceHandlers(new SearchReferenceHandler(queue));
    }
    if (request.getIntermediateResponseHandlers() != null) {
      request.setIntermediateResponseHandlers(
        LdapUtils.concatArrays(
          request.getIntermediateResponseHandlers(),
          new org.ldaptive.handler.IntermediateResponseHandler[] {new IntermediateResponseHandler(queue), }));
    } else {
      request.setIntermediateResponseHandlers(new IntermediateResponseHandler(queue));
    }
  }


  /** Places search entries on a blocking queue. */
  protected class SearchEntryHandler extends AbstractHandler implements org.ldaptive.handler.SearchEntryHandler
  {


    /**
     * Creates a new search entry handler.
     *
     * @param  q  blocking queue
     */
    public SearchEntryHandler(final BlockingQueue<SearchItem> q)
    {
      super(q);
    }


    @Override
    public HandlerResult<SearchEntry> handle(
      final Connection conn,
      final SearchRequest request,
      final SearchEntry entry)
      throws LdapException
    {
      insert(new SearchItem(entry));
      return new HandlerResult<>(null);
    }


    @Override
    public void initializeRequest(final SearchRequest request) {}
  }


  /** Places search references on a blocking queue. */
  protected class SearchReferenceHandler extends AbstractHandler implements org.ldaptive.handler.SearchReferenceHandler
  {


    /**
     * Creates a new search reference handler.
     *
     * @param  q  blocking queue
     */
    public SearchReferenceHandler(final BlockingQueue<SearchItem> q)
    {
      super(q);
    }


    @Override
    public HandlerResult<SearchReference> handle(
      final Connection conn,
      final SearchRequest request,
      final SearchReference reference)
      throws LdapException
    {
      insert(new SearchItem(reference));
      return new HandlerResult<>(null);
    }


    @Override
    public void initializeRequest(final SearchRequest request) {}
  }


  /** Places intermediate responses on a blocking queue. */
  protected class IntermediateResponseHandler extends AbstractHandler
    implements org.ldaptive.handler.IntermediateResponseHandler
  {


    /**
     * Creates a new intermediate response handler.
     *
     * @param  q  blocking queue
     */
    public IntermediateResponseHandler(final BlockingQueue<SearchItem> q)
    {
      super(q);
    }


    @Override
    public HandlerResult<IntermediateResponse> handle(
      final Connection conn,
      final Request request,
      final IntermediateResponse response)
      throws LdapException
    {
      insert(new SearchItem(response));
      return new HandlerResult<>(null);
    }
  }


  /** Common implementation for the handler classes. */
  protected abstract class AbstractHandler
  {

    /** Blocking queue to put items on. */
    private final BlockingQueue<SearchItem> queue;


    /**
     * Creates a new abstract handler.
     *
     * @param  q  blocking queue to put items on
     */
    public AbstractHandler(final BlockingQueue<SearchItem> q)
    {
      queue = q;
    }


    /**
     * Places the supplied item on the blocking queue.
     *
     * @param  item  to insert
     */
    protected void insert(final SearchItem item)
    {
      try {
        logger.debug("received {}", item);
        queue.put(item);
      } catch (Exception e) {
        logger.warn("Unable to insert item {}", item);
      }
    }
  }


  /** Contains data returned when using the {@link QueueingSearchExecutor}. */
  public static class SearchItem
  {

    /** Search entry contained in this blocking search item. */
    private final SearchEntry searchEntry;

    /** Search reference contained in this blocking search item. */
    private final SearchReference searchReference;

    /** Intermediate response contained in this blocking search item. */
    private final IntermediateResponse intermediateResponse;

    /** Response contained in this blocking search item. */
    private final Response<SearchResult> searchResponse;

    /** Exception thrown by the search operation. */
    private final Exception searchException;


    /**
     * Creates a new blocking search item.
     *
     * @param  entry  that represents this item
     */
    public SearchItem(final SearchEntry entry)
    {
      searchEntry = entry;
      searchReference = null;
      intermediateResponse = null;
      searchResponse = null;
      searchException = null;
    }


    /**
     * Creates a new blocking search item.
     *
     * @param  reference  that represents this item
     */
    public SearchItem(final SearchReference reference)
    {
      searchEntry = null;
      searchReference = reference;
      intermediateResponse = null;
      searchResponse = null;
      searchException = null;
    }


    /**
     * Creates a new blocking search item.
     *
     * @param  response  that represents this item
     */
    public SearchItem(final IntermediateResponse response)
    {
      searchEntry = null;
      searchReference = null;
      intermediateResponse = response;
      searchResponse = null;
      searchException = null;
    }


    /**
     * Creates a new blocking search item.
     *
     * @param  response  that represents this item
     */
    public SearchItem(final Response<SearchResult> response)
    {
      searchEntry = null;
      searchReference = null;
      intermediateResponse = null;
      searchResponse = response;
      searchException = null;
    }


    /**
     * Creates a new blocking search item.
     *
     * @param  exception  that represents this item
     */
    public SearchItem(final Exception exception)
    {
      searchEntry = null;
      searchReference = null;
      intermediateResponse = null;
      searchResponse = null;
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
    public SearchEntry getEntry()
    {
      return searchEntry;
    }


    /**
     * Returns whether this item represents a search reference.
     *
     * @return  whether this item represents a search reference
     */
    public boolean isReference()
    {
      return searchReference != null;
    }


    /**
     * Returns the search reference contained in this item or null if this item does not contain a search reference.
     *
     * @return  search entry
     */
    public SearchReference getReference()
    {
      return searchReference;
    }


    /**
     * Returns whether this item represents an intermediate response.
     *
     * @return  whether this item represents an intermediate response
     */
    public boolean isIntermediateResponse()
    {
      return intermediateResponse != null;
    }


    /**
     * Returns the intermediate response contained in this item or null if this item does not contain an intermediate
     * response.
     *
     * @return  intermediate response
     */
    public IntermediateResponse getIntermediateResponse()
    {
      return intermediateResponse;
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
     * Returns the response contained in this item or null if this item does not contain a response.
     *
     * @return  response
     */
    public Response<SearchResult> getResponse()
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
      String s;
      if (isEntry()) {
        s = String.format("[%s@%d::searchEntry=%s]", getClass().getName(), hashCode(), searchEntry);
      } else if (isReference()) {
        s = String.format("[%s@%d::searchReference=%s]", getClass().getName(), hashCode(), searchReference);
      } else if (isIntermediateResponse()) {
        s = String.format("[%s@%d::intermediateResponse=%s]", getClass().getName(), hashCode(), intermediateResponse);
      } else if (isResponse()) {
        s = String.format("[%s@%d::searchResponse=%s]", getClass().getName(), hashCode(), searchResponse);
      } else if (isException()) {
        s = String.format("[%s@%d::searchException=%s]", getClass().getName(), hashCode(), searchException);
      } else {
        s = String.format("[%s@%d]", getClass().getName(), hashCode());
      }
      return s;
    }
  }
}
