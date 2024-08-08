/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.transport.DefaultOperationHandle;

/**
 * Provides handling of an ldap referral for add operations.
 *
 * @author  Middleware Services
 */
public class FollowAddReferralHandler extends AbstractFollowReferralHandler<AddRequest, AddResponse>
  implements ReferralResultHandler<AddResponse>
{


  /** Creates a new add referral handler. */
  public FollowAddReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowAddReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowAddReferralHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowAddReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowAddReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowAddReferralHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new add referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  private FollowAddReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected AddRequest createReferralRequest(final LdapURL url)
  {
    final AddRequest request = AddRequest.builder()
      .controls(getRequest().getControls())
      .dn(!url.getUrl().isDefaultBaseDn() ? url.getUrl().getBaseDn() : getRequest().getDn())
      .attributes(getRequest().getAttributes())
      .build();
    if (getRequest().getResponseTimeout() != null) {
      request.setResponseTimeout(request.getResponseTimeout());
    }
    return request;
  }


  @Override
  protected AddOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultOperationHandle<AddRequest, AddResponse> handle =
      (DefaultOperationHandle<AddRequest, AddResponse>) getHandle();
    final AddOperation op = new AddOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setControlHandlers(handle.getOnControl());
    op.setReferralHandlers(handle.getOnReferral());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setExceptionHandler(handle.getOnException());
    // don't propagate throw condition, it will be enforced on the original operation
    //op.setThrowCondition(handle.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setReferralResultHandler(
      new FollowAddReferralHandler(
        getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory(), getThrowOnFailure()));
    return op;
  }


  @Override
  public FollowAddReferralHandler newInstance()
  {
    return new FollowAddReferralHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
