/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.util.function.Consumer;
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
public class SearchOperationHandle extends OperationHandle
{

  /** Function to handle response entries. */
  private Consumer<SearchResultEntry> onEntry;

  /** Function to handle response references. */
  private Consumer<SearchResultReference> onReference;

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
  public SearchOperationHandle onResult(final Consumer<Result> function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public SearchOperationHandle onControl(final Consumer<ResponseControl> function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public SearchOperationHandle onIntermediate(final Consumer<IntermediateResponse> function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public SearchOperationHandle onUnsolicitedNotification(final Consumer<UnsolicitedNotification> function)
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
   * Sets the function to execute when a search result entry is received.
   *
   * @param  function  to execute on a search result entry
   *
   * @return  this handle
   */
  public SearchOperationHandle onEntry(final Consumer<SearchResultEntry> function)
  {
    onEntry = function;
    return this;
  }


  /**
   * Sets the function to execute when a search result reference is received.
   *
   * @param  function  to execute on a search result reference
   *
   * @return  this handle
   */
  public SearchOperationHandle onReference(final Consumer<SearchResultReference> function)
  {
    onReference = function;
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
      onEntry.accept(r);
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
      onReference.accept(r);
      consumedMessage();
    }
    result.addReference(r);
  }
}
