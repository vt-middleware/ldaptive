/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.LdapException;
import org.ldaptive.OperationHandle;
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
public interface ExtendedOperationHandle extends OperationHandle<ExtendedRequest, ExtendedResponse>
{


  @Override
  ExtendedOperationHandle send();


  @Override
  ExtendedResponse await() throws LdapException;


  @Override
  default ExtendedResponse execute()
    throws LdapException
  {
    return send().await();
  }


  @Override
  ExtendedOperationHandle onResult(ResultHandler... function);


  @Override
  ExtendedOperationHandle onControl(ResponseControlHandler... function);


  @Override
  ExtendedOperationHandle onReferral(ReferralHandler... function);


  @Override
  ExtendedOperationHandle onIntermediate(IntermediateResponseHandler... function);


  @Override
  ExtendedOperationHandle onUnsolicitedNotification(UnsolicitedNotificationHandler... function);


  @Override
  ExtendedOperationHandle onReferralResult(ReferralResultHandler<ExtendedResponse> function);


  @Override
  ExtendedOperationHandle onException(ExceptionHandler function);


  @Override
  ExtendedOperationHandle throwIf(ResultPredicate function);


  @Override
  ExtendedOperationHandle onComplete(CompleteHandler function);


  /**
   * Sets the function to execute when an extended result is received.
   *
   * @param  function  to execute on an extended result
   *
   * @return  this handle
   */
  ExtendedOperationHandle onExtended(ExtendedValueHandler... function);
}
