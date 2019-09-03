/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.Iterator;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.ldaptive.filter.FilterParser;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.provider.DefaultSearchOperationHandle;

/**
 * Provides handling of an ldap continuation reference for search operations.
 *
 * @author  Middleware Services
 */
public class FollowSearchResultReferenceHandler extends AbstractFollowReferralHandler<SearchRequest, SearchResponse>
  implements SearchResultHandler
{


  /** Creates a new search result reference handler. */
  public FollowSearchResultReferenceHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory());
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowSearchResultReferenceHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowSearchResultReferenceHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory());
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowSearchResultReferenceHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private FollowSearchResultReferenceHandler(final int limit, final int depth, final ReferralConnectionFactory factory)
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
      new FollowSearchResultReferenceHandler(
        getReferralLimit(),
        getReferralDepth() + 1,
        getReferralConnectionFactory()));
    return op;
  }


  @Override
  public SearchResponse apply(final SearchResponse result)
  {
    if (result.getReferences() == null || result.getReferences().size() == 0) {
      return result;
    }
    if (referralDepth <= referralLimit) {
      final Iterator<SearchResultReference> i = result.getReferences().iterator();
      while (i.hasNext()) {
        final SearchResultReference ref = i.next();
        i.remove();
        final SearchResponse sr = followReferral(ref.getUris());
        if (sr != null) {
          result.addEntries(sr.getEntries());
          if (sr.getReferralURLs() != null && sr.getReferralURLs().length > 0) {
            result.addReferralURLs(sr.getReferralURLs());
          }
        }
      }
    }
    return result;
  }
}
