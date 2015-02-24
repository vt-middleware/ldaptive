/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.Connection;
import org.ldaptive.LdapURL;
import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.Operation;

/**
 * Provides handling of an ldap referral for modify dn operations.
 *
 * @author  Middleware Services
 */
public class ModifyDnReferralHandler
  extends AbstractReferralHandler<ModifyDnRequest, Void>
{


  /**
   * Creates a new modify dn referral handler.
   */
  public ModifyDnReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new modify dn referral handler.
   *
   * @param  factory  referral connection factory
   */
  public ModifyDnReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 0, factory);
  }


  /**
   * Creates a new modify dn referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public ModifyDnReferralHandler(final int limit)
  {
    this(limit, 0, DEFAULT_CONNECTION_FACTORY);
  }


  /**
   * Creates a new modify dn referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public ModifyDnReferralHandler(
    final int limit,
    final ReferralConnectionFactory factory)
  {
    this(limit, 0, factory);
  }


  /**
   * Creates a new modify dn referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  private ModifyDnReferralHandler(
    final int limit,
    final int depth,
    final ReferralConnectionFactory factory)
  {
    super(limit, depth, factory);
  }


  @Override
  protected ModifyDnRequest createReferralRequest(
    final ModifyDnRequest request,
    final LdapURL url)
  {
    final ModifyDnRequest referralRequest = new ModifyDnRequest();
    referralRequest.setControls(request.getControls());
    referralRequest.setIntermediateResponseHandlers(
      request.getIntermediateResponseHandlers());
    referralRequest.setReferralHandler(
      new ModifyDnReferralHandler(
        getReferralLimit(),
        getReferralDepth() + 1,
        getReferralConnectionFactory()));
    if (!url.getEntry().isDefaultBaseDn()) {
      referralRequest.setDn(url.getEntry().getBaseDn());
    } else {
      referralRequest.setDn(request.getDn());
    }
    referralRequest.setDeleteOldRDn(request.getDeleteOldRDn());
    referralRequest.setNewDn(request.getNewDn());
    return referralRequest;
  }


  @Override
  protected Operation<ModifyDnRequest, Void> createReferralOperation(
    final Connection conn)
  {
    return new ModifyDnOperation(conn);
  }
}
