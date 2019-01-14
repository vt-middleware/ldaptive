/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.util.function.Consumer;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.protocol.CompareRequest;
import org.ldaptive.protocol.IntermediateResponse;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.UnsolicitedNotification;

/**
 * Handle that notifies on the components of a compare request.
 *
 * @author  Middleware Services
 */
public class CompareOperationHandle extends OperationHandle
{

  /** Function to handle the compare result. */
  private Consumer<Boolean> onCompare;


  /**
   * Creates a new compare operation handle.
   *
   * @param  req  compare request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  CompareOperationHandle(final CompareRequest req, final Connection conn, final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  public CompareOperationHandle execute()
  {
    super.execute();
    return this;
  }


  @Override
  public CompareOperationHandle onResult(final Consumer<Result> function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public CompareOperationHandle onControl(final Consumer<ResponseControl> function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public CompareOperationHandle onIntermediate(final Consumer<IntermediateResponse> function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public CompareOperationHandle onUnsolicitedNotification(final Consumer<UnsolicitedNotification> function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public CompareOperationHandle onException(final Consumer<Exception> function)
  {
    super.onException(function);
    return this;
  }


  /**
   * Sets the function to execute when a compare result is received.
   *
   * @param  function  to execute on a compare result
   *
   * @return  this handle
   */
  public CompareOperationHandle onCompare(final Consumer<Boolean> function)
  {
    onCompare = function;
    return this;
  }


  /**
   * Invokes {@link #onCompare}.
   *
   * @param  b  compare result
   */
  void compare(final Boolean b)
  {
    if (onCompare != null) {
      onCompare.accept(b);
    }
  }
}
