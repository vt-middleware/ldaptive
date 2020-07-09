/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.handler.CompareValueHandler;

/**
 * Executes an ldap compare operation.
 *
 * @author  Middleware Services
 */
public class CompareOperation extends AbstractOperation<CompareRequest, CompareResponse>
{

  /** Functions to handle the compare result. */
  private CompareValueHandler[] compareValueHandlers;


  /**
   * Default constructor.
   */
  public CompareOperation() {}


  /**
   * Creates a new compare operation.
   *
   * @param  factory  connection factory
   */
  public CompareOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  public CompareValueHandler[] getCompareValueHandlers()
  {
    return compareValueHandlers;
  }


  public void setCompareValueHandlers(final CompareValueHandler... handlers)
  {
    compareValueHandlers = handlers;
  }


  /**
   * Sends a compare request. See {@link OperationHandle#send()}.
   *
   * @param  request  compare request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public CompareOperationHandle send(final CompareRequest request)
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
   * Sends a compare request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  compare request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static CompareOperationHandle send(final ConnectionFactory factory, final CompareRequest request)
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
   * Executes a compare request. See {@link OperationHandle#execute()}.
   *
   * @param  request  compare request
   *
   * @return  compare result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public CompareResponse execute(final CompareRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(configureRequest(request))).execute();
    }
  }


  /**
   * Executes a compare request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  compare request
   *
   * @return  compare result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static CompareResponse execute(final ConnectionFactory factory, final CompareRequest request)
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
  protected CompareOperationHandle configureHandle(final CompareOperationHandle handle)
  {
    return handle
      .onCompare(getCompareValueHandlers())
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
      .append("compareValueHandlers=").append(Arrays.toString(compareValueHandlers)).toString();
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


  /** Compare operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<CompareOperation.Builder, CompareOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new CompareOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the functions to execute when a compare result is complete.
     *
     * @param  handlers  to execute on a compare result
     *
     * @return  this builder
     */
    public Builder onCompare(final CompareValueHandler... handlers)
    {
      object.setCompareValueHandlers(handlers);
      return self();
    }
  }
}
