/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.handler.CompareValueHandler;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Handle that notifies on the components of a compare request.
 *
 * @author  Middleware Services
 */
public interface CompareOperationHandle extends OperationHandle<CompareRequest, CompareResponse>
{


  @Override
  CompareOperationHandle send();


  @Override
  CompareResponse await() throws LdapException;


  @Override
  default CompareResponse execute()
    throws LdapException
  {
    return send().await();
  }


  @Override
  CompareOperationHandle onResult(ResultHandler... function);


  @Override
  CompareOperationHandle onControl(ResponseControlHandler... function);


  @Override
  CompareOperationHandle onReferral(ReferralHandler... function);


  @Override
  CompareOperationHandle onIntermediate(IntermediateResponseHandler... function);


  @Override
  CompareOperationHandle onUnsolicitedNotification(UnsolicitedNotificationHandler... function);


  @Override
  CompareOperationHandle onReferralResult(ReferralResultHandler<CompareResponse> function);


  @Override
  CompareOperationHandle onException(ExceptionHandler function);


  @Override
  CompareOperationHandle throwIf(ResultPredicate function);


  @Override
  CompareOperationHandle onComplete(CompleteHandler function);


  /**
   * Sets the function to execute when a compare result is received.
   *
   * @param  function  to execute on a compare result
   *
   * @return  this handle
   */
  CompareOperationHandle onCompare(CompareValueHandler... function);
}
