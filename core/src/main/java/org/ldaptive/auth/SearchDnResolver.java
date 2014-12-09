/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;

/**
 * Looks up a user's DN using an LDAP search.
 *
 * @author  Middleware Services
 */
public class SearchDnResolver extends AbstractSearchDnResolver
  implements ConnectionFactoryManager
{

  /** Connection factory. */
  private ConnectionFactory factory;


  /** Default constructor. */
  public SearchDnResolver() {}


  /**
   * Creates a new search dn resolver.
   *
   * @param  cf  connection factory
   */
  public SearchDnResolver(final ConnectionFactory cf)
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
  protected Connection getConnection()
    throws LdapException
  {
    final Connection conn = factory.getConnection();
    conn.open();
    return conn;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, baseDn=%s, userFilter=%s, " +
        "userFilterParameters=%s, allowMultipleDns=%s, subtreeSearch=%s, " +
        "derefAliases=%s, followReferrals=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getBaseDn(),
        getUserFilter(),
        Arrays.toString(getUserFilterParameters()),
        getAllowMultipleDns(),
        getSubtreeSearch(),
        getDerefAliases(),
        getFollowReferrals());
  }
}
