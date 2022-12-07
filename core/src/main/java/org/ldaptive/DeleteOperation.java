/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap delete operation.
 *
 * @author  Middleware Services
 */
public class DeleteOperation extends AbstractOperation<DeleteRequest, DeleteResponse>
{


  /**
   * Default constructor.
   */
  public DeleteOperation() {}


  /**
   * Creates a new delete operation.
   *
   * @param  factory  connection factory
   */
  public DeleteOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  /**
   * Sends a delete request. See {@link OperationHandle#send()}.
   *
   * @param  request  delete request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public OperationHandle<DeleteRequest, DeleteResponse> send(final DeleteRequest request)
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
   * Sends a delete request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  delete request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static OperationHandle<DeleteRequest, DeleteResponse> send(
    final ConnectionFactory factory,
    final DeleteRequest request)
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
   * Executes a delete request. See {@link OperationHandle#execute()}.
   *
   * @param  request  delete request
   *
   * @return  delete result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public DeleteResponse execute(final DeleteRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(configureRequest(request))).execute();
    }
  }


  /**
   * Executes a delete request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  delete request
   *
   * @return  delete result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static DeleteResponse execute(final ConnectionFactory factory, final DeleteRequest request)
    throws LdapException
  {
    try (Connection conn = factory.getConnection()) {
      conn.open();
      return conn.operation(request).execute();
    }
  }


  /**
   * Returns a new delete operation with the same properties as the supplied operation.
   *
   * @param  operation  to copy
   *
   * @return  copy of the supplied delete operation
   */
  public static DeleteOperation copy(final DeleteOperation operation)
  {
    final DeleteOperation op = new DeleteOperation();
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


  /** Delete operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<DeleteOperation.Builder, DeleteOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new DeleteOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
