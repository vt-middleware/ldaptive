/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.handler.IntermediateResponseHandler;

/**
 * Contains the data common to all request objects.
 *
 * @author  Middleware Services
 */
public abstract class AbstractRequest implements Request
{

  /** Request controls. */
  private RequestControl[] controls;

  /** Whether to follow referrals. */
  private boolean followReferrals;

  /** Intermediate response handlers. */
  private IntermediateResponseHandler[] intermediateResponseHandlers;


  @Override
  public RequestControl[] getControls()
  {
    return controls;
  }


  /**
   * Sets the controls for this request.
   *
   * @param  c  controls to set
   */
  public void setControls(final RequestControl... c)
  {
    controls = c;
  }


  @Override
  public boolean getFollowReferrals()
  {
    return followReferrals;
  }


  /**
   * Sets whether to follow referrals.
   *
   * @param  b  whether to follow referrals
   */
  public void setFollowReferrals(final boolean b)
  {
    followReferrals = b;
  }


  @Override
  public IntermediateResponseHandler[] getIntermediateResponseHandlers()
  {
    return intermediateResponseHandlers;
  }


  /**
   * Sets the intermediate response handlers.
   *
   * @param  handlers  intermediate response handlers
   */
  public void setIntermediateResponseHandlers(
    final IntermediateResponseHandler... handlers)
  {
    intermediateResponseHandlers = handlers;
  }
}
