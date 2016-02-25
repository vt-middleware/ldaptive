/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Connection strategy that randomizes the list of configured URLs. A random URL ordering will be created for each
 * connection attempt.
 *
 * @author  Middleware Services
 */
public class RandomConnectionStrategy implements ConnectionStrategy
{


  /**
   * Return a list of URLs in random order.
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

    final List<String> l = Arrays.asList(metadata.getLdapUrl().split(" "));
    Collections.shuffle(l);
    return l.toArray(new String[l.size()]);
  }
}
