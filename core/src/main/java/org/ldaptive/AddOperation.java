/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap add operation.
 *
 * @author  Middleware Services
 */
public class AddOperation extends AbstractOperation<AddRequest, AddResponse>
{


  /**
   * Default constructor.
   */
  public AddOperation() {}


  /**
   * Creates a new add operation.
   *
   * @param  factory  connection factory
   */
  public AddOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  /**
   * Sends an add request. See {@link OperationHandle#send()}.
   *
   * @param  request  add request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public OperationHandle<AddRequest, AddResponse> send(final AddRequest request)
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
   * Sends an add request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  add request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static OperationHandle<AddRequest, AddResponse> send(
    final ConnectionFactory factory,
    final AddRequest request)
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
   * Executes an add request. See {@link OperationHandle#execute()}.
   *
   * @param  request  add request
   *
   * @return  add result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public AddResponse execute(final AddRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(configureRequest(request))).execute();
    }
  }


  /**
   * Executes an add request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  add request
   *
   * @return  add result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static AddResponse execute(final ConnectionFactory factory, final AddRequest request)
    throws LdapException
  {
    try (Connection conn = factory.getConnection()) {
      conn.open();
      return conn.operation(request).execute();
    }
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


  /** Add operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<AddOperation.Builder, AddOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new AddOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
