/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Message;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
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

  /** Predicate that requires any message except unsolicited. */
  private static final Predicate<Message> SEARCH_RESPONSE_TIMEOUT_CONDITION =
    new Predicate<>() {
      @Override
      public boolean test(final Message message)
      {
        return message instanceof IntermediateResponse ||
          message instanceof LdapEntry ||
          message instanceof SearchResultReference ||
          message instanceof SearchResponse;
      }

      @Override
      public String toString()
      {
        return "SEARCH_RESPONSE_TIMEOUT_CONDITION";
      }
    };

  /** Whether to automatically sort search results. */
  private static final boolean SORT_RESULTS = Boolean.parseBoolean(
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
  public DefaultSearchOperationHandle(final SearchRequest req, final TransportConnection conn, final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  protected Predicate<Message> getResponseTimeoutCondition()
  {
    return SEARCH_RESPONSE_TIMEOUT_CONDITION;
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
  public DefaultSearchOperationHandle throwIf(final ResultPredicate function)
  {
    super.throwIf(function);
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
    if (getMessageID() != r.getMessageID()) {
      final IllegalArgumentException e = new IllegalArgumentException("Invalid entry " + r + " for handle " + this);
      exception(new LdapException(e));
      throw e;
    }
    LdapEntry e = r;
    if (onEntry != null) {
      for (LdapEntryHandler func : onEntry) {
        try {
          e = func.apply(e);
        } catch (Exception ex) {
          logger.warn("Entry function {} in handle {} threw an exception", func, this, ex);
        }
      }
    }
    if (e != null) {
      result.addEntries(e);
    }
    consumedMessage(r);
  }


  /**
   * Invokes {@link #onReference}.
   *
   * @param  r  search result reference
   */
  public void reference(final SearchResultReference r)
  {
    if (getMessageID() != r.getMessageID()) {
      final IllegalArgumentException e = new IllegalArgumentException("Invalid reference " + r + " for handle " + this);
      exception(new LdapException(e));
      throw e;
    }
    if (onReference != null) {
      for (SearchReferenceHandler func : onReference) {
        try {
          func.accept(r);
        } catch (Exception ex) {
          logger.warn("Reference consumer {} in handle {} threw an exception", func, this, ex);
        }
      }
    }
    result.addReferences(r);
    consumedMessage(r);
  }


  @Override
  public String toString()
  {
    // do not log the result object, it is not thread safe
    return super.toString() + ", " +
      "onEntry=" + Arrays.toString(onEntry) + ", " +
      "onReference=" + Arrays.toString(onReference) + ", " +
      "onSearchResult=" + Arrays.toString(onSearchResult);
  }
}
