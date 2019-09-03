/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.filter.FilterParser;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.provider.DefaultSearchOperationHandle;

/**
 * Provides handling of an ldap referral for search operations.
 *
 * @author  Middleware Services
 */
public class FollowSearchReferralHandler extends AbstractFollowReferralHandler<SearchRequest, SearchResponse>
  implements SearchResultHandler
{


  /** Creates a new search referral handler. */
  public FollowSearchReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory());
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowSearchReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowSearchReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory());
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowSearchReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private FollowSearchReferralHandler(final int limit, final int depth, final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected SearchRequest createReferralRequest(final LdapURL url)
  {
    return SearchRequest.builder()
      .controls(getRequest().getControls())
      .scope(!url.isDefaultScope() ? url.getScope() : getRequest().getSearchScope())
      .dn(!url.isDefaultBaseDn() ? url.getBaseDn() : getRequest().getBaseDn())
      .filter(
        !url.isDefaultFilter() ?
          FilterParser.parse(url.getFilter().format()) : getRequest().getFilter())
      .sizeLimit(getRequest().getSizeLimit())
      .timeLimit(getRequest().getTimeLimit())
      .typesOnly(getRequest().isTypesOnly())
      .attributes(getRequest().getReturnAttributes())
      .aliases(getRequest().getDerefAliases())
      .binary(getRequest().getBinaryAttributes())
      .build();
  }


  @Override
  protected SearchOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultSearchOperationHandle handle = (DefaultSearchOperationHandle) getHandle();
    final SearchOperation op = new SearchOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setEntryHandlers(handle.getOnEntry());
    op.setReferenceHandlers(handle.getOnReference());
    op.setControlHandlers(handle.getOnControl());
    op.setExceptionHandler(handle.getOnException());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setReferralHandlers(handle.getOnReferral());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setSearchResultHandlers(
      new FollowSearchReferralHandler(getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory()));
    return op;
  }
}
