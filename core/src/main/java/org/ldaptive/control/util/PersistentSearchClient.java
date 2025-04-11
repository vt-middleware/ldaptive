/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.control.PersistentSearchChangeType;
import org.ldaptive.control.PersistentSearchRequestControl;
import org.ldaptive.control.RequestControl;
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

  /** Invoked when an entry is received. */
  private Consumer<LdapEntry> onEntry;

  /** Invoked when a result is received. */
  private Consumer<Result> onResult;

  /** Invoked when an exception is received. */
  private Consumer<Exception> onException;


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
   * Sets the onException consumer.
   *
   * @param  consumer  to invoke when a sync info message is received
   */
  public void setOnException(final Consumer<Exception> consumer)
  {
    onException = consumer;
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
   *
   * @return  search operation handle
   *
   * @throws  LdapException  if the search fails
   */
  public SearchOperationHandle send(final SearchRequest request)
    throws LdapException
  {
    request.setControls(appendRequestControls(request));
    final SearchOperation search = new SearchOperation(factory, request);
    search.setResultHandlers(result -> {
      logger.debug("Received {}", result);
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
      logger.debug("Received exception:", e);
      try {
        onException.accept(e);
      } catch (Exception ex) {
        logger.warn("Unable to process exception", ex);
      }
    });
    search.setEntryHandlers(entry -> {
      logger.debug("Received {}", entry);
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
    handle = search.send();
    return handle;
  }


  /**
   * Invokes an abandon operation on the search handle.
   */
  public void abandon()
  {
    handle.abandon();
  }


  /**
   * Creates a new array of request controls which includes the persistent search request control. Any other request
   * controls are included.
   *
   * @param  request  to read controls from
   *
   * @return  search request controls
   */
  private RequestControl[] appendRequestControls(final SearchRequest request)
  {
    if (request.getControls() != null && request.getControls().length > 0) {
      final List<RequestControl> requestControls = Arrays.stream(request.getControls())
        .filter(c -> !(c instanceof PersistentSearchRequestControl)).collect(Collectors.toCollection(ArrayList::new));
      requestControls.add(new PersistentSearchRequestControl(changeTypes, changesOnly, returnEcs, true));
      return requestControls.toArray(RequestControl[]::new);
    }
    return new RequestControl[] {new PersistentSearchRequestControl(changeTypes, changesOnly, returnEcs, true)};
  }
}
