/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap bind operation.
 *
 * @author  Middleware Services
 */
public class BindOperation extends AbstractOperation<BindRequest, BindResponse>
{


  /**
   * Default constructor.
   */
  public BindOperation() {}


  /**
   * Creates a new bind operation.
   *
   * @param  factory  connection factory
   */
  public BindOperation(final ConnectionFactory factory)
  {
    super(factory);
  }


  /**
   * Sends a bind request. See {@link OperationHandle#send()}.
   *
   * @param  request  bind request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public OperationHandle<BindRequest, BindResponse> send(final BindRequest request)
    throws LdapException
  {
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();
    } catch (Exception e) {
      conn.close();
      throw e;
    }
    return configureHandle(conn.operation(request)).onComplete(conn::close).send();
  }


  /**
   * Sends a bind request. See {@link OperationHandle#send()}.
   *
   * @param  factory  connection factory
   * @param  request  bind request
   *
   * @return  operation handle
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static OperationHandle<BindRequest, BindResponse> send(
    final ConnectionFactory factory,
    final BindRequest request)
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
   * Executes a bind request. See {@link OperationHandle#execute()}.
   *
   * @param  request  bind request
   *
   * @return  bind result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  @Override
  public BindResponse execute(final BindRequest request)
    throws LdapException
  {
    try (Connection conn = getConnectionFactory().getConnection()) {
      conn.open();
      return configureHandle(conn.operation(request)).execute();
    }
  }


  /**
   * Executes a bind request. See {@link OperationHandle#execute()}.
   *
   * @param  factory  connection factory
   * @param  request  bind request
   *
   * @return  bind result
   *
   * @throws  LdapException  if the connection cannot be opened
   */
  public static BindResponse execute(final ConnectionFactory factory, final BindRequest request)
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


  /** Bind operation builder. */
  public static class Builder extends AbstractOperation.AbstractBuilder<BindOperation.Builder, BindOperation>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new BindOperation());
    }


    @Override
    protected Builder self()
    {
      return this;
    }
  }
}
