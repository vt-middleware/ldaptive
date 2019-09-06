/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.time.Duration;
import java.util.Arrays;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.SearchReferenceHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Handle that notifies on the components of a search request.
 *
 * @author  Middleware Services
 */
public class DefaultSearchOperationHandle
  extends DefaultOperationHandle<SearchRequest, SearchResponse> implements SearchOperationHandle
{

  /** Whether to automatically sort search results. */
  private static final boolean SORT_RESULTS = Boolean.valueOf(
    System.getProperty("org.ldaptive.sortSearchResults", "false"));

  /** Functions to handle response entries. */
  private LdapEntryHandler[] onEntry;

  /** Functions to handle response references. */
  private SearchReferenceHandler[] onReference;

  /** Functions to handle complete response. */
  private SearchResultHandler[] onSearchResult;

  /** Synthetic result that is built as entries and references are received. */
  private SearchResponse result = new SearchResponse();


  /**
   * Creates a new search operation handle.
   *
   * @param  req  search request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  public DefaultSearchOperationHandle(final SearchRequest req, final ProviderConnection conn, final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  public DefaultSearchOperationHandle send()
  {
    super.send();
    return this;
  }


  @Override
  public SearchResponse await()
    throws LdapException
  {
    final SearchResponse done = super.await();
    result.initialize(done);
    if (SORT_RESULTS) {
      result = SearchResponse.sort(result);
    }
    if (onSearchResult != null) {
      for (SearchResultHandler func : onSearchResult) {
        try {
          result = func.apply(result);
        } catch (Exception ex) {
          logger.warn("Result function {} threw an exception", func, ex);
        }
      }
    }
    return result;
  }


  @Override
  public SearchResponse execute()
    throws LdapException
  {
    return send().await();
  }


  @Override
  public DefaultSearchOperationHandle onResult(final ResultHandler... function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onControl(final ResponseControlHandler... function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onReferral(final ReferralHandler... function)
  {
    super.onReferral(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onIntermediate(final IntermediateResponseHandler... function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onUnsolicitedNotification(final UnsolicitedNotificationHandler... function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onException(final ExceptionHandler function)
  {
    super.onException(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onComplete(final CompleteHandler function)
  {
    super.onComplete(function);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onEntry(final LdapEntryHandler... function)
  {
    onEntry = function;
    initializeMessageFunctional((Object[]) onEntry);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onReference(final SearchReferenceHandler... function)
  {
    onReference = function;
    initializeMessageFunctional((Object[]) onReference);
    return this;
  }


  @Override
  public DefaultSearchOperationHandle onSearchResult(final SearchResultHandler... function)
  {
    onSearchResult = function;
    initializeMessageFunctional((Object[]) onSearchResult);
    return this;
  }


  public LdapEntryHandler[] getOnEntry()
  {
    return onEntry;
  }


  public SearchReferenceHandler[] getOnReference()
  {
    return onReference;
  }


  public SearchResultHandler[] getOnSearchResult()
  {
    return onSearchResult;
  }


  /**
   * Invokes {@link #onEntry}.
   *
   * @param  r  search result entry
   */
  public void entry(final LdapEntry r)
  {
    LdapEntry e = r;
    if (onEntry != null) {
      for (LdapEntryHandler func : onEntry) {
        try {
          e = func.apply(e);
        } catch (Exception ex) {
          logger.warn("Entry function {} in handle {} threw an exception", func, this, ex);
        }
      }
      consumedMessage();
    }
    if (e != null) {
      result.addEntries(e);
    }
  }


  /**
   * Invokes {@link #onReference}.
   *
   * @param  r  search result reference
   */
  public void reference(final SearchResultReference r)
  {
    if (onReference != null) {
      for (SearchReferenceHandler func : onReference) {
        try {
          func.accept(r.getUris());
        } catch (Exception ex) {
          logger.warn("Reference consumer {} in handle {} threw an exception", func, this, ex);
        }
      }
      consumedMessage();
    }
    result.addReferences(r);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("onEntry=").append(Arrays.toString(onEntry)).append(", ")
      .append("onReference=").append(Arrays.toString(onReference)).append(", ")
      .append("onSearchResult=").append(Arrays.toString(onSearchResult)).append(", ")
      .append("result=").append(result).toString();
  }
}
