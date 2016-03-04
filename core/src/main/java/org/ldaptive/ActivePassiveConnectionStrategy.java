/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Connection strategy that returns URLs ordered exactly the way they are configured. This means that the first URL will
 * always be attempted first, followed by each URL in the list.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategy implements ConnectionStrategy
{


  /**
   * Return the URLs in the order they are provided, so that the first URL is always tried first, then the second, and
   * so forth.
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

    return metadata.getLdapUrl().split(" ");
  }
}
