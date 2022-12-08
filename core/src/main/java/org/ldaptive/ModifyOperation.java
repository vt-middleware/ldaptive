/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap modify operation.
 *
 * @author  Middleware Services
 */
public class ModifyOperation extends AbstractOperation<ModifyRequest, ModifyResponse>
{


  /**
   * Default constructor.
   */
  public ModifyOperation() {}


  /**
   * Creates a new modify operation.
   *
   * @param  factory  connection factory
   */
  public ModifyOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  /**
   * Sends a modify request. See {@link OperationHandle#send()}.
   *
   * @param  request  modify request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public OperationHandle<ModifyRequest, ModifyResponse> send(final ModifyRequest request)
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
   * Sends a modify request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  modify request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static OperationHandle<ModifyRequest, ModifyResponse> send(
    final ConnectionFactory factory,
    final ModifyRequest request)
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
   * Executes a modify request. See {@link OperationHandle#execute()}.
   *
   * @param  request  modify request
   *
   * @return  modify result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public ModifyResponse execute(final ModifyRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(configureRequest(request))).execute();
    }
  }


  /**
   * Executes a modify request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  modify request
   *
   * @return  modify result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static ModifyResponse execute(final ConnectionFactory factory, final ModifyRequest request)
    throws LdapException
  {
    try (Connection conn = factory.getConnection()) {
      conn.open();
      return conn.operation(request).execute();
    }
  }


  /**
   * Returns a new modify operation with the same properties as the supplied operation.
   *
   * @param  operation  to copy
   *
   * @return  copy of the supplied modify operation
   */
  public static ModifyOperation copy(final ModifyOperation operation)
  {
    final ModifyOperation op = new ModifyOperation();
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


  /** Modify operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<ModifyOperation.Builder, ModifyOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new ModifyOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
