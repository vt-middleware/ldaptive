/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.time.Duration;
import java.util.Arrays;
import org.ldaptive.LdapException;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.ExtendedValueHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Handle that notifies on the components of an extended request.
 *
 * @author  Middleware Services
 */
public final class DefaultExtendedOperationHandle
  extends DefaultOperationHandle<ExtendedRequest, ExtendedResponse> implements ExtendedOperationHandle
{

  /** Functions to handle extended response name and value. */
  private ExtendedValueHandler[] onExtended;


  /**
   * Creates a new extended operation handle.
   *
   * @param  req  search request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  public DefaultExtendedOperationHandle(
    final ExtendedRequest req,
    final TransportConnection conn,
    final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  public DefaultExtendedOperationHandle send()
  {
    super.send();
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onResult(final ResultHandler... function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onControl(final ResponseControlHandler... function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onReferral(final ReferralHandler... function)
  {
    super.onReferral(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onIntermediate(final IntermediateResponseHandler... function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onUnsolicitedNotification(final UnsolicitedNotificationHandler... function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onReferralResult(final ReferralResultHandler<ExtendedResponse> function)
  {
    super.onReferralResult(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onException(final ExceptionHandler function)
  {
    super.onException(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle throwIf(final ResultPredicate function)
  {
    super.throwIf(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onComplete(final CompleteHandler function)
  {
    super.onComplete(function);
    return this;
  }


  @Override
  public DefaultExtendedOperationHandle onExtended(final ExtendedValueHandler... function)
  {
    onExtended = initializeMessageFunctional(function);
    return this;
  }


  public ExtendedValueHandler[] getOnExtended()
  {
    return onExtended;
  }


  /**
   * Invokes {@link #onExtended}.
   *
   * @param  response  extended response
   */
  public void extended(final ExtendedResponse response)
  {
    if (getMessageID() != response.getMessageID()) {
      final IllegalArgumentException e = new IllegalArgumentException(
        "Invalid extended response " + response + " for handle " + this);
      notifyExceptionHandlers(new LdapException(e));
      throw e;
    }
    if (onExtended != null) {
      for (ExtendedValueHandler func : onExtended) {
        try {
          func.accept(response.getResponseName(), response.getResponseValue());
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " + "onExtended=" + Arrays.toString(onExtended);
  }
}
