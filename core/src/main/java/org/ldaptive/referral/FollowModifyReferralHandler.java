/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.transport.DefaultOperationHandle;

/**
 * Provides handling of an ldap referral for modify operations.
 *
 * @author  Middleware Services
 */
public class FollowModifyReferralHandler extends AbstractFollowReferralHandler<ModifyRequest, ModifyResponse>
  implements ReferralResultHandler<ModifyResponse>
{


  /** Creates a new modify referral handler. */
  public FollowModifyReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowModifyReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowModifyReferralHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowModifyReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowModifyReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowModifyReferralHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new modify referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  private FollowModifyReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected ModifyRequest createReferralRequest(final LdapURL url)
  {
    final ModifyRequest request = ModifyRequest.builder()
      .controls(getRequest().getControls())
      .dn(!url.getUrl().isDefaultBaseDn() ? url.getUrl().getBaseDn() : getRequest().getDn())
      .modifications(getRequest().getModifications())
      .build();
    if (getRequest().getResponseTimeout() != null) {
      request.setResponseTimeout(request.getResponseTimeout());
    }
    return request;
  }


  @Override
  protected ModifyOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultOperationHandle<ModifyRequest, ModifyResponse> handle =
      (DefaultOperationHandle<ModifyRequest, ModifyResponse>) getHandle();
    final ModifyOperation op = new ModifyOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setControlHandlers(handle.getOnControl());
    op.setReferralHandlers(handle.getOnReferral());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setExceptionHandler(handle.getOnException());
    // don't propagate throw condition, it will be enforced on the original operation
    //op.setThrowCondition(handle.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setReferralResultHandler(
      new FollowModifyReferralHandler(
        getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory(), getThrowOnFailure()));
    return op;
  }


  @Override
  public FollowModifyReferralHandler newInstance()
  {
    return new FollowModifyReferralHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
