/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.Arrays;
import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.OperationHandle;
import org.ldaptive.handler.ExtendedValueHandler;

/**
 * Executes an ldap extended operation.
 *
 * @author  Middleware Services
 */
public class ExtendedOperation extends AbstractOperation<ExtendedRequest, ExtendedResponse>
{

  /** Function to handle extended response data. */
  private ExtendedValueHandler[] extendedValueHandlers;


  /**
   * Default constructor.
   */
  public ExtendedOperation() {}


  /**
   * Creates a new extended operation.
   *
   * @param  factory  connection factory
   */
  public ExtendedOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  public ExtendedValueHandler[] getExtendedValueHandlers()
  {
    return extendedValueHandlers;
  }


  public void setExtendedValueHandlers(final ExtendedValueHandler... handlers)
  {
    extendedValueHandlers = handlers;
  }


  /**
   * Sends an extended request. See {@link OperationHandle#send()}.
   *
   * @param  request  extended request
   *
   * @return  operation handle
   *
   * @throws LdapException  if the connection cannot be opened
   */
  @Override
  public ExtendedOperationHandle send(final ExtendedRequest request)
    throws LdapException
  {
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    return configureHandle(conn.operation(configureRequest(request))).onComplete(conn::close).send();
  }


  /**
   * Sends an extended request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  extended request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static ExtendedOperationHandle send(final ConnectionFactory factory, final ExtendedRequest request)
    throws LdapException
  {
    final Connection conn = factory.getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    return conn.operation(request).onComplete(conn::close).send();
  }


  /**
   * Executes an extended request. See {@link OperationHandle#execute()}.
   *
   * @param  request  extended request
   *
   * @return  extended result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public ExtendedResponse execute(final ExtendedRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(configureRequest(request))).execute();
    }
  }


  /**
   * Executes an extended request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  extended request
   *
   * @return  extended result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static ExtendedResponse execute(final ConnectionFactory factory, final ExtendedRequest request)
    throws LdapException
  {
    try (Connection conn = factory.getConnection()) {
      conn.open();
      return conn.operation(request).execute();
    }
  }


  /**
   * Adds configured functions to the supplied handle.
   *
   * @param  handle  to configure
   *
   * @return  configured handle
   */
  protected ExtendedOperationHandle configureHandle(final ExtendedOperationHandle handle)
  {
    return handle
      .onExtended(getExtendedValueHandlers())
      .onControl(getControlHandlers())
      .onReferral(getReferralHandlers())
      .onIntermediate(getIntermediateResponseHandlers())
      .onException(getExceptionHandler())
      .throwIf(getThrowCondition())
      .onUnsolicitedNotification(getUnsolicitedNotificationHandlers())
      .onResult(getResultHandlers());
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("extendedValueHandlers=").append(Arrays.toString(extendedValueHandlers)).toString();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  /** Extended operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<ExtendedOperation.Builder, ExtendedOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new ExtendedOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the functions to execute when an extended result is complete.
     *
     * @param  handlers  to execute on an extended result
     *
     * @return  this builder
     */
    public Builder onExtended(final ExtendedValueHandler... handlers)
    {
      object.setExtendedValueHandlers(handlers);
      return self();
    }
  }
}
