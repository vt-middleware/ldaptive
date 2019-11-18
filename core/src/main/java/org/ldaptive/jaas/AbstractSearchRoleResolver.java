/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Set;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.auth.AbstractSearchOperationFactory;

/**
 * Base class for search role resolver implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchRoleResolver extends AbstractSearchOperationFactory implements RoleResolver
{


  @Override
  public Set<LdapRole> search(final SearchRequest request)
    throws LdapException
  {
    try (Connection conn = getConnection()) {
      final SearchOperation op = createSearchOperation(conn);
      return LdapRole.toRoles(op.execute(request).getResult());
    }
  }


  /**
   * Retrieve a connection that is ready for use.
   *
   * @return  connection
   *
   * @throws  LdapException  if an error occurs opening the connection
   */
  protected abstract Connection getConnection()
    throws LdapException;
}
