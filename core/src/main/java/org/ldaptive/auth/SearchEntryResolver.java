/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResponse;

/**
 * Looks up the LDAP entry associated with a user. If a connection factory is configured it will be used to perform the
 * search for user. The connection will be opened and closed for each resolution. If no connection factory is configured
 * the search will occur using the connection that the bind was attempted on.
 *
 * @author  Middleware Services
 */
public class SearchEntryResolver extends AbstractSearchEntryResolver implements ConnectionFactoryManager
{


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
  public SearchResponse performLdapSearch(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    if (getConnectionFactory() == null) {
      return response.getConnection().operation(createSearchRequest(criteria)).execute();
    } else {
      final SearchOperation op = createSearchOperation();
      return op.execute(createSearchRequest(criteria));
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("factory=").append(getConnectionFactory()).append(", ")
      .append("baseDn=").append(getBaseDn()).append(", ")
      .append("userFilter=").append(getUserFilter()).append(", ")
      .append("userFilterParameters=").append(Arrays.toString(getUserFilterParameters())).append(", ")
      .append("allowMultipleEntries=").append(getAllowMultipleEntries()).append(", ")
      .append("subtreeSearch=").append(getSubtreeSearch()).append(", ")
      .append("derefAliases=").append(getDerefAliases()).append(", ")
      .append("binaryAttributes=").append(Arrays.toString(getBinaryAttributes())).append(", ")
      .append("entryHandlers=").append(Arrays.toString(getEntryHandlers())).append("]").toString();
  }
}
