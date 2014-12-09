/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap add operation.
 *
 * @author  Middleware Services
 */
public class AddOperation extends AbstractOperation<AddRequest, Void>
{


  /**
   * Creates a new add operation.
   *
   * @param  conn  connection
   */
  public AddOperation(final Connection conn)
  {
    super(conn);
  }


  @Override
  protected Response<Void> invoke(final AddRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().add(request);
  }
}
