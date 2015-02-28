/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapURL;
import org.ldaptive.Operation;

/**
 * Provides handling of an ldap referral for add operations.
 *
 * @author  Middleware Services
 */
public class AddReferralHandler
  extends AbstractReferralHandler<AddRequest, Void>
{


  /**
   * Creates a new add referral handler.
   */
  public AddReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  factory  referral connection factory
   */
  public AddReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, factory);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public AddReferralHandler(final int limit)
  {
    this(limit, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public AddReferralHandler(
    final int limit,
    final ReferralConnectionFactory factory)
  {
    this(limit, 0, factory);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private AddReferralHandler(
    final int limit,
    final int depth,
    final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected AddRequest createReferralRequest(
    final AddRequest request,
    final LdapURL url)
  {
    final AddRequest referralRequest = new AddRequest();
    referralRequest.setControls(request.getControls());
    referralRequest.setIntermediateResponseHandlers(
      request.getIntermediateResponseHandlers());
    referralRequest.setReferralHandler(
      new AddReferralHandler(
        getReferralLimit(),
        getReferralDepth() + 1,
        getReferralConnectionFactory()));
    if (!url.getEntry().isDefaultBaseDn()) {
      referralRequest.setDn(url.getEntry().getBaseDn());
    } else {
      referralRequest.setDn(request.getDn());
    }
    referralRequest.setLdapAttributes(request.getLdapAttributes());
    return referralRequest;
  }


  @Override
  protected Operation<AddRequest, Void> createReferralOperation(
    final Connection conn)
  {
    return new AddOperation(conn);
  }
}
