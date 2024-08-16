/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.Set;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapURL;
import org.ldaptive.ResultCode;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.transport.DefaultCompareOperationHandle;

/**
 * Provides handling of an ldap referral for compare operations.
 *
 * @author  Middleware Services
 */
public class FollowCompareReferralHandler extends AbstractFollowReferralHandler<CompareRequest, CompareResponse>
  implements ReferralResultHandler<CompareResponse>
{


  /** Creates a new compare referral handler. */
  public FollowCompareReferralHandler()
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  factory  referral connection factory
   */
  public FollowCompareReferralHandler(final ReferralConnectionFactory factory)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, false);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowCompareReferralHandler(final ReferralConnectionFactory factory, final boolean tf)
  {
    this(DEFAULT_REFERRAL_LIMIT, 1, factory, tf);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   */
  public FollowCompareReferralHandler(final int limit)
  {
    this(limit, 1, new DefaultReferralConnectionFactory(), false);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   */
  public FollowCompareReferralHandler(final int limit, final ReferralConnectionFactory factory)
  {
    this(limit, 1, factory, false);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  public FollowCompareReferralHandler(final int limit, final ReferralConnectionFactory factory, final boolean tf)
  {
    this(limit, 1, factory, tf);
  }


  /**
   * Creates a new compare referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referrals
   */
  private FollowCompareReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    super(limit, depth, factory, tf);
  }


  @Override
  protected CompareRequest createReferralRequest(final LdapURL url)
  {
    final CompareRequest request = CompareRequest.builder()
      .controls(getRequest().getControls())
      .dn(!url.getUrl().isDefaultBaseDn() ? url.getUrl().getBaseDn() : getRequest().getDn())
      .name(getRequest().getName())
      .value(getRequest().getValue())
      .build();
    if (getRequest().getResponseTimeout() != null) {
      request.setResponseTimeout(request.getResponseTimeout());
    }
    return request;
  }


  @Override
  protected CompareOperation createReferralOperation(final ConnectionFactory factory)
  {
    final DefaultCompareOperationHandle handle = (DefaultCompareOperationHandle) getHandle();
    final CompareOperation op = new CompareOperation(factory);
    op.setResultHandlers(handle.getOnResult());
    op.setControlHandlers(handle.getOnControl());
    op.setReferralHandlers(handle.getOnReferral());
    op.setIntermediateResponseHandlers(handle.getOnIntermediate());
    op.setExceptionHandler(handle.getOnException());
    // don't propagate throw condition, it will be enforced on the original operation
    //op.setThrowCondition(handle.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(handle.getOnUnsolicitedNotification());
    op.setCompareValueHandlers(handle.getOnCompare());
    op.setReferralResultHandler(
      new FollowCompareReferralHandler(
        getReferralLimit(), getReferralDepth() + 1, getReferralConnectionFactory(), getThrowOnFailure()));
    return op;
  }


  @Override
  protected Set<ResultCode> getSuccessResultCodes()
  {
    return Set.of(ResultCode.COMPARE_FALSE, ResultCode.COMPARE_TRUE);
  }


  @Override
  public FollowCompareReferralHandler newInstance()
  {
    return new FollowCompareReferralHandler(
      getReferralLimit(), getReferralDepth(), getReferralConnectionFactory(), getThrowOnFailure());
  }
}
