/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * Looks up a user's roles using a pool of connections.
 *
 * @author  Middleware Services
 */
public class PooledSearchRoleResolver extends AbstractSearchRoleResolver
  implements PooledConnectionFactoryManager
{

  /** Connection factory. */
  private PooledConnectionFactory factory;


  /** Default constructor. */
  public PooledSearchRoleResolver() {}


  /**
   * Creates a new pooled role resolver.
   *
   * @param  cf  connection factory
   */
  public PooledSearchRoleResolver(final PooledConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  @Override
  public PooledConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  /**
   * Sets the connection factory.
   *
   * @param  cf  connection factory
   */
  @Override
  public void setConnectionFactory(final PooledConnectionFactory cf)
  {
    factory = cf;
  }


  /** {@inheritDoc} */
  @Override
  protected Connection getConnection()
    throws LdapException
  {
    return factory.getConnection();
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s]",
        getClass().getName(),
        hashCode(),
        factory);
  }
}
