/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

/**
 * Connection strategy that returns the exact URL that is returned from {@link
 * ConnectionFactoryMetadata#getLdapUrl()}.
 *
 * @author  Middleware Services
 */
public class DefaultConnectionStrategy implements ConnectionStrategy
{


  /**
   * Returns an array containing a single entry URL obtained from {@link
   * ConnectionFactoryMetadata#getLdapUrl()}.
   *
   * @param  metadata  which can be used to produce the URL list
   *
   * @return  list of URLs to attempt connections to
   */
  @Override
  public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
  {
    if (metadata == null || metadata.getLdapUrl() == null) {
      return null;
    }
    return new String[] {metadata.getLdapUrl()};
  }
}
