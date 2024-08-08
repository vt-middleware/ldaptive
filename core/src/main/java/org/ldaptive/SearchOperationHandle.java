/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ReferralResultHandler;
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
public interface SearchOperationHandle extends OperationHandle<SearchRequest, SearchResponse>
{


  @Override
  SearchOperationHandle send();


  @Override
  SearchResponse await() throws LdapException;


  @Override
  default SearchResponse execute()
    throws LdapException
  {
    return send().await();
  }


  @Override
  SearchOperationHandle onResult(ResultHandler... function);


  @Override
  SearchOperationHandle onControl(ResponseControlHandler... function);


  @Override
  SearchOperationHandle onReferral(ReferralHandler... function);


  @Override
  SearchOperationHandle onIntermediate(IntermediateResponseHandler... function);


  @Override
  SearchOperationHandle onUnsolicitedNotification(UnsolicitedNotificationHandler... function);


  @Override
  SearchOperationHandle onReferralResult(ReferralResultHandler<SearchResponse> function);


  @Override
  SearchOperationHandle onException(ExceptionHandler function);


  @Override
  SearchOperationHandle onComplete(CompleteHandler function);


  @Override
  SearchOperationHandle throwIf(ResultPredicate function);


  /**
   * Sets the functions to execute when a search result entry is received.
   *
   * @param  function  to execute on a search result entry
   *
   * @return  this handle
   */
  SearchOperationHandle onEntry(LdapEntryHandler... function);


  /**
   * Sets the functions to execute when a search result reference is received.
   *
   * @param  function  to execute on a search result reference
   *
   * @return  this handle
   */
  SearchOperationHandle onReference(SearchReferenceHandler... function);


  /**
   * Sets the functions to execute when a search result is complete.
   *
   * @param  function  to execute on a search result
   *
   * @return  this handle
   */
  SearchOperationHandle onSearchResult(SearchResultHandler... function);
}
