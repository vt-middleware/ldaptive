/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap delete operation.
 *
 * @author  Middleware Services
 */
public class DeleteOperation extends AbstractOperation<DeleteRequest, Void>
{


  /**
   * Creates a new delete operation.
   *
   * @param  conn  connection
   */
  public DeleteOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<Void> invoke(final DeleteRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().delete(request);
  }
}
