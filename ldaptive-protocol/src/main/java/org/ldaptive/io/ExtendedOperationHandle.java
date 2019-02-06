/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.protocol.ExtendedRequest;
import org.ldaptive.protocol.ExtendedResponse;
import org.ldaptive.protocol.IntermediateResponse;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.UnsolicitedNotification;

/**
 * Handle that notifies on the components of an extended request.
 *
 * @author  Middleware Services
 */
public class ExtendedOperationHandle extends OperationHandle<ExtendedRequest>
{

  /** Functions to handle extended response name and value. */
  private BiConsumer<String, byte[]>[] onExtended;


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
  public ExtendedOperationHandle send()
  {
    super.send();
    return this;
  }


  @Override
  public ExtendedResponse await()
    throws LdapException
  {
    return (ExtendedResponse) super.await();
  }


  @Override
  public ExtendedResponse execute()
    throws LdapException
  {
    return send().await();
  }


  @Override
  public ExtendedOperationHandle closeOnComplete()
  {
    super.closeOnComplete();
    return this;
  }


  @Override
  public ExtendedOperationHandle onResult(final Consumer<Result>... function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onControl(final Consumer<ResponseControl>... function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onIntermediate(final Consumer<IntermediateResponse>... function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onUnsolicitedNotification(final Consumer<UnsolicitedNotification>... function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public ExtendedOperationHandle onException(final Consumer<LdapException> function)
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
  public ExtendedOperationHandle onExtended(final BiConsumer<String, byte[]>... function)
  {
    onExtended = function;
    initializeMessageFunctional(onExtended);
    return this;
  }


  /**
   * Invokes {@link #onExtended}.
   *
   * @param  response  extended response
   */
  void extended(final ExtendedResponse response)
  {
    if (onExtended != null) {
      for (BiConsumer<String, byte[]> func : onExtended) {
        func.accept(response.getResponseName(), response.getResponseValue());
      }
    }
  }
}
