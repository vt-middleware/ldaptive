/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
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
 * @version  $Revision: 3056 $ $Date: 2014-09-09 16:01:06 -0400 (Tue, 09 Sep 2014) $
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


  /** {@inheritDoc} */
  @Override
  public PooledConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  /** {@inheritDoc} */
  @Override
  public void setConnectionFactory(final PooledConnectionFactory cf)
  {
    factory = cf;
  }


  /** {@inheritDoc} */
  @Override
  protected SearchResult performLdapSearch(
    final Connection conn,
    final AuthenticationCriteria ac)
    throws LdapException
  {
    Connection pooledConn = null;
    try {
      pooledConn = factory.getConnection();

      final SearchOperation op = createSearchOperation(pooledConn);
      return op.execute(createSearchRequest(ac)).getResult();
    } finally {
      if (pooledConn != null) {
        pooledConn.close();
      }
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, baseDn=%s, userFilter=%s, " +
        "userFilterParameters=%s, allowMultipleEntries=%s, " +
        "subtreeSearch=%s, derefAliases=%s, followReferrals=%s, " +
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
        getFollowReferrals(),
        Arrays.toString(getSearchEntryHandlers()));
  }
}
