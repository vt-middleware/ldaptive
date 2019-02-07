/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.control.PersistentSearchChangeType;
import org.ldaptive.control.PersistentSearchRequestControl;
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

  /** Connection factory to get a connection from. */
  private final ConnectionFactory factory;

  /** Change types. */
  private final EnumSet<PersistentSearchChangeType> changeTypes;

  /** Whether to return only changed entries. */
  private final boolean changesOnly;

  /** Whether to return an Entry Change Notification control. */
  private final boolean returnEcs;

  /** Search operation handle. */
  private SearchOperationHandle handle;


  /**
   * Creates a new persistent search client.
   *
   * @param  cf  to get a connection from
   * @param  types  persistent search change types
   * @param  co  whether only changed entries are returned
   * @param  re  return an Entry Change Notification control
   */
  public PersistentSearchClient(
    final ConnectionFactory cf,
    final EnumSet<PersistentSearchChangeType> types,
    final boolean co,
    final boolean re)
  {
    factory = cf;
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
  public BlockingQueue<PersistentSearchItem> execute(final SearchRequest request, final int capacity)
    throws LdapException
  {
    final BlockingQueue<PersistentSearchItem> queue = new LinkedBlockingQueue<>(capacity);

    request.setControls(new PersistentSearchRequestControl(changeTypes, changesOnly, returnEcs, true));
    final SearchOperation search = new SearchOperation(factory, request);
    search.setResultHandlers(result -> {
      logger.debug("received {}", result);
      try {
        queue.put(new PersistentSearchItem(result));
      } catch (InterruptedException e) {
        logger.warn("Unable to enqueue result {}", result);
      }
    });
    search.setExceptionHandler(e -> {
      logger.debug("received exception:", e);
      try {
        queue.put(new PersistentSearchItem(e));
      } catch (InterruptedException e1) {
        logger.warn("Unable to enqueue exception", e);
      }
    });
    search.setEntryHandlers(entry -> {
      logger.debug("received {}", entry);
      final PersistentSearchItem item = new PersistentSearchItem(new PersistentSearchItem.Entry(entry));
      try {
        queue.put(item);
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
}
