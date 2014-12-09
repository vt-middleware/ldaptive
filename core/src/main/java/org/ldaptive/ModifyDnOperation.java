/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap modify dn operation.
 *
 * @author  Middleware Services
 */
public class ModifyDnOperation extends AbstractOperation<ModifyDnRequest, Void>
{


  /**
   * Creates a new modify dn operation.
   *
   * @param  conn  connection
   */
  public ModifyDnOperation(final Connection conn)
  {
    super(conn);
  }


  @Override
  protected Response<Void> invoke(final ModifyDnRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().modifyDn(request);
  }
}
