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
  public OperationHandle<ModifyDnRequest, ModifyDnResponse> send(final ModifyDnRequest request)
    throws LdapException
  {
    final Connection conn = getConnectionFactory().getConnection();
    conn.open();
    return configureHandle(conn.operation(request)).closeOnComplete().send();
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
    conn.open();
    return conn.operation(request).closeOnComplete().send();
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
      return configureHandle(conn.operation(request)).execute();
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
