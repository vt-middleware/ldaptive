/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapURL;
import org.ldaptive.Operation;

/**
 * Provides handling of an ldap referral for compare operations.
 *
 * @author  Middleware Services
 */
public class CompareReferralHandler extends AbstractReferralHandler<CompareRequest, Boolean>
{


  /** Creates a compare add referral handler. */
  public CompareReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  factory  referral connection factory
   */
  public CompareReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, factory);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public CompareReferralHandler(final int limit)
  {
    this(limit, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public CompareReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 0, factory);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private CompareReferralHandler(final int limit, final int depth, final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected CompareRequest createReferralRequest(final CompareRequest request, final LdapURL url)
  {
    final CompareRequest referralRequest = new CompareRequest();
    referralRequest.setControls(request.getControls());
    referralRequest.setIntermediateResponseHandlers(request.getIntermediateResponseHandlers());
    referralRequest.setReferralHandler(
      new CompareReferralHandler(getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory()));
    if (!url.getEntry().isDefaultBaseDn()) {
      referralRequest.setDn(url.getEntry().getBaseDn());
    } else {
      referralRequest.setDn(request.getDn());
    }
    referralRequest.setAttribute(request.getAttribute());
    return referralRequest;
  }


  @Override
  protected Operation<CompareRequest, Boolean> createReferralOperation(final Connection conn)
  {
    return new CompareOperation(conn);
  }
}
