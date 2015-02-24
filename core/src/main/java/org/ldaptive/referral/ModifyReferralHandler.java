/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.Connection;
import org.ldaptive.LdapURL;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Operation;

/**
 * Provides handling of an ldap referral for modify operations.
 *
 * @author  Middleware Services
 */
public class ModifyReferralHandler
  extends AbstractReferralHandler<ModifyRequest, Void>
{


  /**
   * Creates a new modify referral handler.
   */
  public ModifyReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  factory  referral connection factory
   */
  public ModifyReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, factory);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public ModifyReferralHandler(final int limit)
  {
    this(limit, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public ModifyReferralHandler(
    final int limit,
    final ReferralConnectionFactory factory)
  {
    this(limit, 0, factory);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private ModifyReferralHandler(
    final int limit,
    final int depth,
    final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected ModifyRequest createReferralRequest(
    final ModifyRequest request,
    final LdapURL url)
  {
    final ModifyRequest referralRequest = new ModifyRequest();
    referralRequest.setControls(request.getControls());
    referralRequest.setIntermediateResponseHandlers(
      request.getIntermediateResponseHandlers());
    referralRequest.setReferralHandler(
      new ModifyReferralHandler(
        getReferralLimit(),
        getReferralDepth() + 1,
        getReferralConnectionFactory()));
    if (!url.getEntry().isDefaultBaseDn()) {
      referralRequest.setDn(url.getEntry().getBaseDn());
    } else {
      referralRequest.setDn(request.getDn());
    }
    referralRequest.setAttributeModifications(
      request.getAttributeModifications());
    return referralRequest;
  }


  @Override
  protected Operation<ModifyRequest, Void> createReferralOperation(
    final Connection conn)
  {
    return new ModifyOperation(conn);
  }
}
