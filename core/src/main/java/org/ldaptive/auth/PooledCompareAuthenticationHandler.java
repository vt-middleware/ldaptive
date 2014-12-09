/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * Provides an LDAP authentication implementation that leverages a pool of ldap
 * connections to perform the compare operation against the userPassword
 * attribute. The default password scheme used is 'SHA'.
 *
 * @author  Middleware Services
 */
public class PooledCompareAuthenticationHandler
  extends AbstractCompareAuthenticationHandler
  implements PooledConnectionFactoryManager
{

  /** Connection factory. */
  private PooledConnectionFactory factory;


  /** Default constructor. */
  public PooledCompareAuthenticationHandler() {}


  /**
   * Creates a new pooled compare authentication handler.
   *
   * @param  cf  connection factory
   */
  public PooledCompareAuthenticationHandler(final PooledConnectionFactory cf)
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
        "[%s@%d::factory=%s, passwordScheme=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getPasswordScheme(),
        Arrays.toString(getAuthenticationControls()));
  }
}
