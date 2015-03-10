/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * Looks up a user's DN using a pool of connections.
 *
 * @author  Middleware Services
 */
public class PooledSearchDnResolver extends AbstractSearchDnResolver implements PooledConnectionFactoryManager
{

  /** Connection factory. */
  private PooledConnectionFactory factory;


  /** Default constructor. */
  public PooledSearchDnResolver() {}


  /**
   * Creates a new pooled search dn resolver.
   *
   * @param  cf  connection factory
   */
  public PooledSearchDnResolver(final PooledConnectionFactory cf)
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
  protected Connection getConnection()
    throws LdapException
  {
    return factory.getConnection();
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, baseDn=%s, userFilter=%s, " +
        "userFilterParameters=%s, allowMultipleDns=%s, subtreeSearch=%s, " +
        "derefAliases=%s, referralHandler=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getBaseDn(),
        getUserFilter(),
        Arrays.toString(getUserFilterParameters()),
        getAllowMultipleDns(),
        getSubtreeSearch(),
        getDerefAliases(),
        getReferralHandler());
  }
}
