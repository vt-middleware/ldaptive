/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import org.ldaptive.protocol.Request;

/**
 * Base class for processing a message that is initialized with the request and connection.
 *
 * @param  <T> type of request
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
public abstract class MessageFunctional<T extends Request>
{

  /** Connection the request occurred on. */
  private Connection connection;

  /** Request that produced the message. */
  private T request;


  public Connection getConnection()
  {
    return connection;
  }


  public T getRequest()
  {
    return request;
  }


  public void setConnection(final Connection conn)
  {
    connection = conn;
  }


  public void setRequest(final T req)
  {
    request = req;
  }


  /**
   * Marker class to inject connection and request properties.
   *
   * @param  <Q>  type of request
   * @param  <T>  the type of the input to the function
   * @param  <R>  the type of the result of the function
   */
  public abstract static class Function<Q extends Request, T, R>
    extends MessageFunctional<Q> implements java.util.function.Function<T, R> {}


  /**
   * Marker class to inject connection and request properties.
   *
   * @param  <Q>  type of request
   * @param  <T>  the type of the input to the operation
   */
  public abstract static class Consumer<Q extends Request, T>
    extends MessageFunctional<Q> implements java.util.function.Consumer<T> {}


  /**
   * Marker class to inject connection and request properties.
   *
   * @param  <Q>  type of request
   * @param  <T>  the type of the first argument to the operation
   * @param  <U>  the type of the second argument to the operation
   */
  public abstract static class BiConsumer<Q extends Request, T, U>
    extends MessageFunctional<Q> implements java.util.function.BiConsumer<T, U> {}
}
// CheckStyle:AbstractClassName ON
