/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.protocol.ExtendedRequest;
import org.ldaptive.protocol.IntermediateResponse;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.UnsolicitedNotification;

/**
 * Handle that notifies on the components of an extended request.
 *
 * @author  Middleware Services
 */
public class ExtendedOperationHandle extends OperationHandle
{

  /** Function to handle extended response name and value. */
  private BiConsumer<String, byte[]> onExtended;


  /**
   * Creates a new extended operation handle.
   *
   * @param  req  search request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  ExtendedOperationHandle(final ExtendedRequest req, final Connection conn, final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  public ExtendedOperationHandle execute()
  {
    super.execute();
    return this;
  }


  @Override
  public ExtendedOperationHandle onResult(final Consumer<Result> function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onControl(final Consumer<ResponseControl> function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onIntermediate(final Consumer<IntermediateResponse> function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onUnsolicitedNotification(final Consumer<UnsolicitedNotification> function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onException(final Consumer<Exception> function)
  {
    super.onException(function);
    return this;
  }


  /**
   * Sets the function to execute when an extended response is received.
   *
   * @param  function  to execute on an extended response
   *
   * @return  this handle
   */
  public ExtendedOperationHandle onExtended(final BiConsumer<String, byte[]> function)
  {
    onExtended = function;
    return this;
  }


  /**
   * Invokes {@link #onExtended}.
   *
   * @param  name  of the extended response
   * @param  value  of the extended response
   */
  void extended(final String name, final byte[] value)
  {
    if (onExtended != null) {
      onExtended.accept(name, value);
    }
  }
}
