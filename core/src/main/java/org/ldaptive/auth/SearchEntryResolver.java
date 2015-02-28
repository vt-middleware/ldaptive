/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResult;

/**
 * Looks up the LDAP entry associated with a user. If a connection factory is
 * configured it will be used to perform the search for user. The connection
 * will be opened and closed for each resolution. If no connection factory is
 * configured the search will occur using the connection that the bind was
 * attempted on.
 *
 * @author  Middleware Services
 */
public class SearchEntryResolver extends AbstractSearchEntryResolver
  implements ConnectionFactoryManager
{

  /** Connection factory. */
  private ConnectionFactory factory;


  /** Default constructor. */
  public SearchEntryResolver() {}


  /**
   * Creates a new search entry resolver.
   *
   * @param  cf  connection factory
   */
  public SearchEntryResolver(final ConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  @Override
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  @Override
  public void setConnectionFactory(final ConnectionFactory cf)
  {
    factory = cf;
  }


  @Override
  public SearchResult performLdapSearch(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    if (factory == null) {
      final SearchOperation op = createSearchOperation(
        response.getConnection());
      return op.execute(createSearchRequest(criteria)).getResult();
    } else {
      try (Connection factoryConn = factory.getConnection()) {
        factoryConn.open();

        final SearchOperation op = createSearchOperation(factoryConn);
        return op.execute(createSearchRequest(criteria)).getResult();
      }
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
