/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResult;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * Looks up the LDAP entry associated with a user using a pool of LDAP
 * connections. Resolution will not occur using the connection that the user
 * attempted to bind on.
 *
 * @author  Middleware Services
 */
public class PooledSearchEntryResolver extends AbstractSearchEntryResolver
  implements PooledConnectionFactoryManager
{

  /** Connection factory. */
  private PooledConnectionFactory factory;


  /** Default constructor. */
  public PooledSearchEntryResolver() {}


  /**
   * Creates a new pooled search entry resolver.
   *
   * @param  cf  connection factory
   */
  public PooledSearchEntryResolver(final PooledConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  @Override
  public PooledConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  @Override
  public void setConnectionFactory(final PooledConnectionFactory cf)
  {
    factory = cf;
  }


  @Override
  protected SearchResult performLdapSearch(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    try (Connection pooledConn = factory.getConnection()) {
      final SearchOperation op = createSearchOperation(pooledConn);
      return op.execute(createSearchRequest(criteria)).getResult();
    }
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, baseDn=%s, userFilter=%s, " +
        "userFilterParameters=%s, allowMultipleEntries=%s, " +
        "subtreeSearch=%s, derefAliases=%s, referralHandler=%s, " +
        "searchEntryHandlers=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getBaseDn(),
        getUserFilter(),
        Arrays.toString(getUserFilterParameters()),
        getAllowMultipleEntries(),
        getSubtreeSearch(),
        getDerefAliases(),
        getReferralHandler(),
        Arrays.toString(getSearchEntryHandlers()));
  }
}
