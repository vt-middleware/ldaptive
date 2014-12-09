/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Executes an ldap compare operation.
 *
 * @author  Middleware Services
 */
public class CompareOperation extends AbstractOperation<CompareRequest, Boolean>
{


  /**
   * Creates a new compare operation.
   *
   * @param  conn  connection
   */
  public CompareOperation(final Connection conn)
  {
    super(conn);
  }


  @Override
  protected Response<Boolean> invoke(final CompareRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().compare(request);
  }
}
