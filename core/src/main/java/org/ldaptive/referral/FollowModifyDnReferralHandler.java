/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.transport.DefaultOperationHandle;

/**
 * Provides handling of an ldap referral for modify dn operations.
 *
 * @author  Middleware Services
 */
public class FollowModifyDnReferralHandler extends AbstractFollowReferralHandler<ModifyDnRequest, ModifyDnResponse>
  implements ReferralResultHandler<ModifyDnResponse>
{


  /** Creates a new modify DN referral handler. */
  public FollowModifyDnReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new modify DN referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowModifyDnReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new modify DN referral handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowModifyDnReferralHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new modify DN referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowModifyDnReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new modify DN referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowModifyDnReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new modify DN referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowModifyDnReferralHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new modify DN referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  private FollowModifyDnReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected ModifyDnRequest createReferralRequest(final LdapURL url)
  {
    final ModifyDnRequest request = ModifyDnRequest.builder()
      .controls(getRequest().getControls())
      .oldDN(!url.getUrl().isDefaultBaseDn() ? url.getUrl().getBaseDn() : getRequest().getOldDn())
      .newRDN(getRequest().getNewRDn())
      .delete(getRequest().isDeleteOldRDn())
      .superior(getRequest().getNewSuperiorDn())
      .build();
    if (getRequest().getResponseTimeout() != null) {
      request.setResponseTimeout(request.getResponseTimeout());
    }
    return request;
  }


  @Override
  protected ModifyDnOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultOperationHandle<ModifyDnRequest, ModifyDnResponse> handle =
      (DefaultOperationHandle<ModifyDnRequest, ModifyDnResponse>) getHandle();
    final ModifyDnOperation op = new ModifyDnOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setControlHandlers(handle.getOnControl());
    op.setReferralHandlers(handle.getOnReferral());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setExceptionHandler(handle.getOnException());
    // don't propagate throw condition, it will be enforced on the original operation
    //op.setThrowCondition(handle.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setReferralResultHandler(
      new FollowModifyDnReferralHandler(
        getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory(), getThrowOnFailure()));
    return op;
  }


  @Override
  public FollowModifyDnReferralHandler newInstance()
  {
    return new FollowModifyDnReferralHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
