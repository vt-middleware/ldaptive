/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Set;
import org.ldaptive.AbstractSearchOperationFactory;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;

/**
 * Base class for search role resolver implementations.
 *
 * @author  Middleware Services
 */
public class SearchRoleResolver extends AbstractSearchOperationFactory implements RoleResolver
{


  /** Default constructor. */
  public SearchRoleResolver() {}


  /**
   * Creates a new role resolver.
   *
   * @param  cf  connection factory
   */
  public SearchRoleResolver(final ConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  @Override
  public Set<LdapRole> search(final SearchRequest request)
    throws LdapException
  {
    final SearchOperation op = createSearchOperation();
    final SearchResponse result = op.execute(request);
    if (!result.isSuccess()) {
      throw new LdapException("Unsuccessful role search: " + result);
    }
    return LdapRole.toRoles(result);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("factory=").append(getConnectionFactory()).append("]").toString();
  }
}
