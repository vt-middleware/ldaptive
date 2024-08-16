/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.ldaptive.handler.SearchResultHandler;

/**
 * Provides handling of an ldap continuation reference for search operations.
 *
 * @author  Middleware Services
 */
public class FollowSearchResultReferenceHandler extends AbstractFollowSearchReferralHandler
  implements SearchResultHandler
{


  /** Creates a new search result reference handler. */
  public FollowSearchResultReferenceHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowSearchResultReferenceHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase references
   */
  public FollowSearchResultReferenceHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of references to follow
   */
  public FollowSearchResultReferenceHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of references to follow
   * @param  factory  referral connection factory
   */
  public FollowSearchResultReferenceHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of references to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase references
   */
  public FollowSearchResultReferenceHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new search result reference handler.
   *
   * @param  limit  number of references to follow
   * @param  depth  number of references followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase references
   */
  private FollowSearchResultReferenceHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected FollowSearchResultReferenceHandler createNextSearchResultHandler()
  {
    return new FollowSearchResultReferenceHandler(
      getReferralLimit(),
      getReferralDepth() + 1,
      getReferralConnectionFactory(),
      getThrowOnFailure());
  }


  @Override
  public SearchResponse apply(final SearchResponse result)
  {
    if (!result.isSuccess() || result.getReferences().isEmpty()) {
      return result;
    }
    if (referralDepth > referralLimit) {
      throw new RuntimeException(
        new LdapException(ResultCode.REFERRAL_LIMIT_EXCEEDED, "Referral limit of " + referralLimit + " exceeded"));
    }
    final SearchResponse referralResult = SearchResponse.copy(result);
    final List<SearchResultReference> refsToAdd = new ArrayList<>();
    final List<SearchResultReference> refsToRemove = new ArrayList<>();
    for (SearchResultReference ref : referralResult.getReferences()) {
      final SearchResponse sr;
      try {
        sr = followReferral(ref.getUris());
      } catch (LdapException e) {
        throw new RuntimeException(e);
      }
      if (sr != null && sr.getResultCode() == ResultCode.SUCCESS) {
        refsToRemove.add(ref);
        sr.getEntries().forEach(e -> referralResult.addEntries(LdapEntry.copy(e)));
        sr.getReferences().forEach(r -> refsToAdd.add(SearchResultReference.copy(r)));
      } else if (getThrowOnFailure()) {
        throw new RuntimeException(
          new LdapException(ResultCode.LOCAL_ERROR, "Could not follow referral " + referralResult));
      }
    }
    refsToRemove.forEach(referralResult::removeReferences);
    refsToAdd.forEach(referralResult::addReferences);
    return referralResult;
  }


  @Override
  public FollowSearchResultReferenceHandler newInstance()
  {
    return new FollowSearchResultReferenceHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
