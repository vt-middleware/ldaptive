/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.referral.ReferralHandler;

/**
 * Contains the data common to all request objects.
 *
 * @author  Middleware Services
 */
public abstract class AbstractRequest implements Request
{

  /** Request controls. */
  private RequestControl[] controls;

  /** Referral handler. */
  private ReferralHandler referralHandler;

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
  public ReferralHandler getReferralHandler()
  {
    return referralHandler;
  }


  /**
   * Sets the referral handler.
   *
   * @param  handler  referral handler
   */
  @SuppressWarnings("unchecked")
  public void setReferralHandler(final ReferralHandler handler)
  {
    if (handler != null) {
      handler.initializeRequest(this);
    }
    referralHandler = handler;
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
