/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.Connection;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapURL;
import org.ldaptive.Operation;

/**
 * Provides handling of an ldap referral for delete operations.
 *
 * @author  Middleware Services
 */
public class DeleteReferralHandler
  extends AbstractReferralHandler<DeleteRequest, Void>
{


  /**
   * Creates a new delete referral handler.
   */
  public DeleteReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  factory  referral connection factory
   */
  public DeleteReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, factory);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public DeleteReferralHandler(final int limit)
  {
    this(limit, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public DeleteReferralHandler(
    final int limit,
    final ReferralConnectionFactory factory)
  {
    this(limit, 0, factory);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private DeleteReferralHandler(
    final int limit,
    final int depth,
    final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected DeleteRequest createReferralRequest(
    final DeleteRequest request,
    final LdapURL url)
  {
    final DeleteRequest referralRequest = new DeleteRequest();
    referralRequest.setControls(request.getControls());
    referralRequest.setIntermediateResponseHandlers(
      request.getIntermediateResponseHandlers());
    referralRequest.setReferralHandler(
      new DeleteReferralHandler(
        getReferralLimit(),
        getReferralDepth() + 1,
        getReferralConnectionFactory()));
    if (!url.getEntry().isDefaultBaseDn()) {
      referralRequest.setDn(url.getEntry().getBaseDn());
    } else {
      referralRequest.setDn(request.getDn());
    }
    return referralRequest;
  }


  @Override
  protected Operation<DeleteRequest, Void> createReferralOperation(
    final Connection conn)
  {
    return new DeleteOperation(conn);
  }
}
