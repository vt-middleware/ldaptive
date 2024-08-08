/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.SearchResponse;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.handler.SearchResultHandler;

/**
 * Provides handling of an ldap referral for search operations.
 *
 * @author  Middleware Services
 */
public class FollowSearchReferralHandler extends AbstractFollowSearchReferralHandler
  implements SearchResultHandler, ReferralResultHandler<SearchResponse>
{


  /** Creates a new search referral handler. */
  public FollowSearchReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowSearchReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowSearchReferralHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowSearchReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowSearchReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowSearchReferralHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new search referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  private FollowSearchReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected FollowSearchReferralHandler createNextSearchResultHandler()
  {
    return new FollowSearchReferralHandler(
      getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory(), getThrowOnFailure());
  }


  @Override
  public FollowSearchReferralHandler newInstance()
  {
    return new FollowSearchReferralHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
