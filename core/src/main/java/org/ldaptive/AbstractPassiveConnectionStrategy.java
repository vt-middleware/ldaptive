/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Base class for connection strategies that implement an active/passive format for URLs. This format indicates that the
 * first URL in the list is the most desirable and should be attempted first. Followed by URLs that are less desirable.
 *
 * @author  Middleware Services
 */
public abstract class AbstractPassiveConnectionStrategy extends AbstractConnectionStrategy
{


  @Override
  public void populate(final String urls, final LdapURLSet urlSet)
  {
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("urls cannot be empty or null");
    }
    if (urls.contains(" ")) {
      final String[] urlArray = urls.split(" ");
      urlSet.populate(IntStream.range(0, urlArray.length)
        .mapToObj(i -> {
          final LdapURL url = new LdapURL(urlArray[i]);
          url.setRetryMetadata(new LdapURLRetryMetadata(this));
          url.setPriority(i);
          return url;
        }).collect(Collectors.toList()));
    } else {
      final LdapURL url = new LdapURL(urls);
      url.setRetryMetadata(new LdapURLRetryMetadata(this));
      urlSet.populate(Collections.singletonList(url));
    }
  }
}
