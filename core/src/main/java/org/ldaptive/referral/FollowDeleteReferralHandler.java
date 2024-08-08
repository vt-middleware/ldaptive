/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapURL;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.transport.DefaultOperationHandle;

/**
 * Provides handling of an ldap referral for delete operations.
 *
 * @author  Middleware Services
 */
public class FollowDeleteReferralHandler extends AbstractFollowReferralHandler<DeleteRequest, DeleteResponse>
  implements ReferralResultHandler<DeleteResponse>
{


  /** Creates a new delete referral handler. */
  public FollowDeleteReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowDeleteReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowDeleteReferralHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowDeleteReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowDeleteReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowDeleteReferralHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new delete referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  private FollowDeleteReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected DeleteRequest createReferralRequest(final LdapURL url)
  {
    final DeleteRequest request = DeleteRequest.builder()
      .controls(getRequest().getControls())
      .dn(!url.getUrl().isDefaultBaseDn() ? url.getUrl().getBaseDn() : getRequest().getDn())
      .build();
    if (getRequest().getResponseTimeout() != null) {
      request.setResponseTimeout(request.getResponseTimeout());
    }
    return request;
  }


  @Override
  protected DeleteOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultOperationHandle<DeleteRequest, DeleteResponse> handle =
      (DefaultOperationHandle<DeleteRequest, DeleteResponse>) getHandle();
    final DeleteOperation op = new DeleteOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setControlHandlers(handle.getOnControl());
    op.setReferralHandlers(handle.getOnReferral());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setExceptionHandler(handle.getOnException());
    // don't propagate throw condition, it will be enforced on the original operation
    //op.setThrowCondition(handle.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setReferralResultHandler(
      new FollowDeleteReferralHandler(
        getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory(), getThrowOnFailure()));
    return op;
  }


  @Override
  public FollowDeleteReferralHandler newInstance()
  {
    return new FollowDeleteReferralHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
