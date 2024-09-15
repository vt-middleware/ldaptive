/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.transport.DefaultSearchOperationHandle;

/**
 * Base class with functionality for handling an ldap referral for search operations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractFollowSearchReferralHandler
  extends AbstractFollowReferralHandler<SearchRequest, SearchResponse>
{


  /**
   * Creates a new abstract follow search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public AbstractFollowSearchReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected SearchRequest createReferralRequest(final LdapURL url)
  {
    final SearchRequest request = SearchRequest.copy(getRequest());
    if (!url.getUrl().isDefaultScope()) {
      request.setSearchScope(url.getUrl().getScope());
    }
    if (!url.getUrl().isDefaultBaseDn()) {
      request.setBaseDn(url.getUrl().getBaseDn());
    }
    if (!url.getUrl().isDefaultFilter()) {
      request.setFilter(url.getUrl().getParsedFilter());
    }
    return request;
  }


  @Override
  protected SearchOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultSearchOperationHandle handle = (DefaultSearchOperationHandle) getHandle();
    final SearchOperation op = new SearchOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setControlHandlers(handle.getOnControl());
    op.setReferralHandlers(handle.getOnReferral());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setExceptionHandler(handle.getOnException());
    // don't propagate throw condition, it will be enforced on the original operation
    //op.setThrowCondition(handle.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setEntryHandlers(handle.getOnEntry());
    op.setReferenceHandlers(handle.getOnReference());
    // don't propagate referral result handler, referral chasing is done with a search result handler here
    //op.setReferralResultHandler(handle.getOnReferralResult());
    op.setSearchResultHandlers(createNextSearchResultHandler());
    return op;
  }


  /**
   * Creates the next search result handler for chasing referrals, which increments the referral depth.
   *
   * @return  search result handler
   */
  protected abstract SearchResultHandler[] createNextSearchResultHandler();
}
