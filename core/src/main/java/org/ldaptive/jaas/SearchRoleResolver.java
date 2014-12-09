/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;

/**
 * Looks up a user's roles using an LDAP search.
 *
 * @author  Middleware Services
 */
public class SearchRoleResolver extends AbstractSearchRoleResolver
  implements ConnectionFactoryManager
{

  /** Connection factory. */
  private ConnectionFactory factory;


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


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  @Override
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  /**
   * Sets the connection factory.
   *
   * @param  cf  connection factory
   */
  @Override
  public void setConnectionFactory(final ConnectionFactory cf)
  {
    factory = cf;
  }


  /**
   * Retrieve a connection that is ready for use.
   *
   * @return  connection
   *
   * @throws  LdapException  if an error occurs opening the connection
   */
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
        "[%s@%d::factory=%s]",
        getClass().getName(),
        hashCode(),
        factory);
  }
}
