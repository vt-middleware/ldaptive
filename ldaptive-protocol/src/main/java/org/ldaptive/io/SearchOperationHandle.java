/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.protocol.IntermediateResponse;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.SearchRequest;
import org.ldaptive.protocol.SearchResult;
import org.ldaptive.protocol.SearchResultDone;
import org.ldaptive.protocol.SearchResultEntry;
import org.ldaptive.protocol.SearchResultReference;
import org.ldaptive.protocol.UnsolicitedNotification;

/**
 * Handle that notifies on the components of a search request.
 *
 * @author  Middleware Services
 */
public class SearchOperationHandle extends OperationHandle<SearchRequest>
{

  /** Functions to handle response entries. */
  private Function<SearchResultEntry, SearchResultEntry>[] onEntry;

  /** Functions to handle response references. */
  private Consumer<SearchResultReference>[] onReference;

  /** Synthetic result that is built as entries and references are received. */
  private SearchResult result = new SearchResult();


  /**
   * Creates a new search operation handle.
   *
   * @param  req  search request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  SearchOperationHandle(final SearchRequest req, final Connection conn, final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  public SearchOperationHandle send()
  {
    super.send();
    return this;
  }


  @Override
  public SearchResult await()
    throws LdapException
  {
    final SearchResultDone done = (SearchResultDone) super.await();
    result.initialize(done);
    return result;
  }


  @Override
  public SearchResult execute()
    throws LdapException
  {
    return send().await();
  }


  @Override
  public SearchOperationHandle closeOnComplete()
  {
    super.closeOnComplete();
    return this;
  }


  @Override
  public SearchOperationHandle onResult(final Consumer<Result>... function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public SearchOperationHandle onControl(final Consumer<ResponseControl>... function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public SearchOperationHandle onIntermediate(final Consumer<IntermediateResponse>... function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public SearchOperationHandle onUnsolicitedNotification(final Consumer<UnsolicitedNotification>... function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public SearchOperationHandle onException(final Consumer<LdapException> function)
  {
    super.onException(function);
    return this;
  }


  /**
   * Sets the functions to execute when a search result entry is received.
   *
   * @param  function  to execute on a search result entry
   *
   * @return  this handle
   */
  public SearchOperationHandle onEntry(final Function<SearchResultEntry, SearchResultEntry>... function)
  {
    onEntry = function;
    initializeMessageFunctional(onEntry);
    return this;
  }


  /**
   * Sets the functions to execute when a search result reference is received.
   *
   * @param  function  to execute on a search result reference
   *
   * @return  this handle
   */
  public SearchOperationHandle onReference(final Consumer<SearchResultReference>... function)
  {
    onReference = function;
    initializeMessageFunctional(onReference);
    return this;
  }


  /**
   * Invokes {@link #onEntry}.
   *
   * @param  r  search result entry
   */
  void entry(final SearchResultEntry r)
  {
    if (onEntry != null) {
      SearchResultEntry e = r;
      for (Function<SearchResultEntry, SearchResultEntry> func : onEntry) {
        e = func.apply(e);
      }
      consumedMessage();
    }
    result.addEntry(r);
  }


  /**
   * Invokes {@link #onReference}.
   *
   * @param  r  search result reference
   */
  void reference(final SearchResultReference r)
  {
    if (onReference != null) {
      for (Consumer<SearchResultReference> func : onReference) {
        func.accept(r);
      }
      consumedMessage();
    }
    result.addReference(r);
  }
}
