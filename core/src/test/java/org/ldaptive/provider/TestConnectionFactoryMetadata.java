/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

/**
 * Helper class for connection strategy tests.
 *
 * @author  Middleware Services
 */
public class TestConnectionFactoryMetadata implements ConnectionFactoryMetadata
{

  /** ldap url. */
  private final String ldapUrl;

  /** connection count. */
  private final int connectionCount;


  /**
   * Creates a new test connection factory metadata.
   */
  public TestConnectionFactoryMetadata()
  {
    this(null, 0);
  }


  /**
   * Creates a new test connection factory metadata.
   *
   * @param  url  ldap url
   */
  public TestConnectionFactoryMetadata(final String url)
  {
    this(url, 0);
  }


  /**
   * Creates a new test connection factory metadata.
   *
   * @param  url  ldap url
   * @param  count  connection count
   */
  public TestConnectionFactoryMetadata(final String url, final int count)
  {
    ldapUrl = url;
    connectionCount = count;
  }


  /** {@inheritDoc} */
  @Override
  public String getLdapUrl()
  {
    return ldapUrl;
  }


  /** {@inheritDoc} */
  @Override
  public int getConnectionCount()
  {
    return connectionCount;
  }
}
