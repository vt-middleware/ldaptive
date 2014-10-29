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
package org.ldaptive.pool;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;

/**
 * Leverages a pool to obtain connections for performing ldap operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PooledConnectionFactory implements ConnectionFactory
{

  /** Connection pool. */
  private ConnectionPool pool;


  /** Default constructor. */
  public PooledConnectionFactory() {}


  /**
   * Creates a new pooled connection factory.
   *
   * @param  cp  connection pool
   */
  public PooledConnectionFactory(final ConnectionPool cp)
  {
    pool = cp;
  }


  /**
   * Returns the connection pool.
   *
   * @return  connection pool
   */
  public ConnectionPool getConnectionPool()
  {
    return pool;
  }


  /**
   * Sets the connection pool.
   *
   * @param  cp  connection pool
   */
  public void setConnectionPool(final ConnectionPool cp)
  {
    pool = cp;
  }


  /**
   * Returns a connection from the pool. Connections returned from this method
   * are ready to perform ldap operations.
   *
   * @return  connection
   *
   * @throws  LdapException  if a connection cannot be retrieved from the pool
   */
  @Override
  public Connection getConnection()
    throws LdapException
  {
    return pool.getConnection();
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format("[%s@%d::pool=%s]", getClass().getName(), hashCode(), pool);
  }
}
