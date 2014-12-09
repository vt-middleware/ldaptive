/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;

/**
 * Provides an LDAP authentication implementation that leverages a compare
 * operation against the userPassword attribute. The default password scheme
 * used is 'SHA'.
 *
 * @author  Middleware Services
 */
public class CompareAuthenticationHandler
  extends AbstractCompareAuthenticationHandler
  implements ConnectionFactoryManager
{

  /** Connection factory. */
  private ConnectionFactory factory;


  /** Default constructor. */
  public CompareAuthenticationHandler() {}


  /**
   * Creates a new compare authentication handler.
   *
   * @param  cf  connection factory
   */
  public CompareAuthenticationHandler(final ConnectionFactory cf)
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
        "[%s@%d::factory=%s, passwordScheme=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getPasswordScheme(),
        Arrays.toString(getAuthenticationControls()));
  }
}
