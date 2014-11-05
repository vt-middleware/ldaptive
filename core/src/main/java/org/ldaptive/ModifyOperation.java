/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap modify operation.
 *
 * @author  Middleware Services
 */
public class ModifyOperation extends AbstractOperation<ModifyRequest, Void>
{


  /**
   * Creates a new modify operation.
   *
   * @param  conn  connection
   */
  public ModifyOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<Void> invoke(final ModifyRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().modify(request);
  }
}
