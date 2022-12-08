/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap modify DN operation.
 *
 * @author  Middleware Services
 */
public class ModifyDnOperation extends AbstractOperation<ModifyDnRequest, ModifyDnResponse>
{


  /**
   * Default constructor.
   */
  public ModifyDnOperation() {}


  /**
   * Creates a new modify DN operation.
   *
   * @param  factory  connection factory
   */
  public ModifyDnOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  /**
   * Sends a modify DN request. See {@link OperationHandle#send()}.
   *
   * @param  request  modify DN request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public OperationHandle<ModifyDnRequest, ModifyDnResponse> send(final ModifyDnRequest request)
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
   * Sends a modify DN request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  modify DN request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static OperationHandle<ModifyDnRequest, ModifyDnResponse> send(
    final ConnectionFactory factory,
    final ModifyDnRequest request)
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
   * Executes a modify DN request. See {@link OperationHandle#execute()}.
   *
   * @param  request  modify DN request
   *
   * @return  modify DN result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public ModifyDnResponse execute(final ModifyDnRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(configureRequest(request))).execute();
    }
  }


  /**
   * Executes a modify DN request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  modify dn request
   *
   * @return  modify dn result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static ModifyDnResponse execute(final ConnectionFactory factory, final ModifyDnRequest request)
    throws LdapException
  {
    try (Connection conn = factory.getConnection()) {
      conn.open();
      return conn.operation(request).execute();
    }
  }


  /**
   * Returns a new modify dn operation with the same properties as the supplied operation.
   *
   * @param  operation  to copy
   *
   * @return  copy of the supplied modify dn operation
   */
  public static ModifyDnOperation copy(final ModifyDnOperation operation)
  {
    final ModifyDnOperation op = new ModifyDnOperation();
    op.setRequestHandlers(operation.getRequestHandlers());
    op.setResultHandlers(operation.getResultHandlers());
    op.setControlHandlers(operation.getControlHandlers());
    op.setReferralHandlers(operation.getReferralHandlers());
    op.setIntermediateResponseHandlers(operation.getIntermediateResponseHandlers());
    op.setExceptionHandler(operation.getExceptionHandler());
    op.setThrowCondition(operation.getThrowCondition());
    op.setUnsolicitedNotificationHandlers(operation.getUnsolicitedNotificationHandlers());
    op.setConnectionFactory(operation.getConnectionFactory());
    return op;
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


  /** Modify DN operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<ModifyDnOperation.Builder, ModifyDnOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new ModifyDnOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
