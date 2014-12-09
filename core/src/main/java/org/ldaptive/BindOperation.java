/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap bind operation.
 *
 * @author  Middleware Services
 */
public class BindOperation extends AbstractOperation<BindRequest, Void>
{


  /**
   * Creates a new bind operation.
   *
   * @param  conn  connection
   */
  public BindOperation(final Connection conn)
  {
    super(conn);
  }


  @Override
  protected Response<Void> invoke(final BindRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().bind(request);
  }
}
