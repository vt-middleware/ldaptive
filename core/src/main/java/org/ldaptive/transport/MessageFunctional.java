/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import org.ldaptive.AbstractFreezable;
import org.ldaptive.Connection;
import org.ldaptive.OperationHandle;
import org.ldaptive.Request;
import org.ldaptive.Result;

/**
 * Base class for processing a message that is initialized with properties from the handle.
 *
 * @param  <Q> type of request
 * @param  <S> type of result
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
public abstract class MessageFunctional<Q extends Request, S extends Result> extends AbstractFreezable
{

  /** Connection the request occurred on. */
  private Connection connection;

  /** Request that produced the message. */
  private Q request;

  /** Operation handle that sent the request. */
  private OperationHandle<Q, S> handle;


  /**
   * Returns the connection.
   *
   * @return  connection
   */
  public Connection getConnection()
  {
    return connection;
  }


  /**
   * Sets the connection.
   *
   * @param  conn  connection
   */
  public void setConnection(final Connection conn)
  {
    assertMutable();
    connection = conn;
  }


  /**
   * Returns the request.
   *
   * @return  request
   */
  public Q getRequest()
  {
    return request;
  }


  /**
   * Sets the request.
   *
   * @param  req  request
   */
  public void setRequest(final Q req)
  {
    assertMutable();
    request = req;
  }


  /**
   * Returns the handle.
   *
   * @return  handle
   */
  public OperationHandle<Q, S> getHandle()
  {
    return handle;
  }


  /**
   * Sets the handle.
   *
   * @param  h  handle
   */
  public void setHandle(final OperationHandle<Q, S> h)
  {
    assertMutable();
    handle = h;
  }


  /**
   * Create a new instance of this message functional.
   *
   * @return  new instance of this message functional
   */
  public abstract MessageFunctional<Q, S> newInstance();


  /**
   * Marker class to inject handle properties.
   *
   * @param  <Q>  type of request
   * @param  <S>  type of result
   * @param  <T>  the type of the input to the function
   * @param  <R>  the type of the result of the function
   */
  public abstract static class Function<Q extends Request, S extends Result, T, R>
    extends MessageFunctional<Q, S> implements java.util.function.Function<T, R> {}


  /**
   * Marker class to inject handle properties.
   *
   * @param  <Q>  type of request
   * @param  <S>  type of result
   * @param  <T>  the type of the input to the operation
   */
  public abstract static class Consumer<Q extends Request, S extends Result, T>
    extends MessageFunctional<Q, S> implements java.util.function.Consumer<T> {}


  /**
   * Marker class to inject handle properties.
   *
   * @param  <Q>  type of request
   * @param  <S>  type of result
   * @param  <T>  the type of the first argument to the operation
   * @param  <U>  the type of the second argument to the operation
   */
  public abstract static class BiConsumer<Q extends Request, S extends Result, T, U>
    extends MessageFunctional<Q, S> implements java.util.function.BiConsumer<T, U> {}
}
// CheckStyle:AbstractClassName ON
