/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connection strategy that tries all IP addresses resolved from DNS. The order of IP addressees returned can be
 * controlled via the java.net.preferIPv4Stack or java.net.preferIPv6Addresses system property flags.
 *
 * @author  Middleware Services
 */
public class DnsAllNamesConnectionStrategy extends AbstractConnectionStrategy
{

  /** Custom iterator function. */
  private final Function<List<LdapURL>, Iterator<LdapURL>> iterFunction;


  /** Default constructor. */
  public DnsAllNamesConnectionStrategy()
  {
    this(null);
  }


  /**
   * Creates a new DNS connection strategy.
   *
   * @param  function  that produces a custom iterator
   */
  public DnsAllNamesConnectionStrategy(final Function<List<LdapURL>, Iterator<LdapURL>> function)
  {
    iterFunction = function;
  }


  @Override
  public Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    if (iterFunction != null) {
      return iterFunction.apply(ldapURLSet.getUrls());
    }
    return new DefaultLdapURLIterator(ldapURLSet.getUrls());
  }


  @Override
  public void populate(final String urls, final LdapURLSet urlSet)
  {
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("urls cannot be empty or null");
    }
    if (urls.contains(" ")) {
      urlSet.populate(Stream.of(urls.split(" "))
        .flatMap(s -> {
          final List<LdapURL> l = new ArrayList<>(2);
          try {
            final LdapURL parsedUrl = new LdapURL(s);
            for (InetAddress address : InetAddress.getAllByName(parsedUrl.getHostname())) {
              final LdapURL url = LdapURL.copy(parsedUrl);
              url.setRetryMetadata(new LdapURLRetryMetadata(this));
              url.setIpAddress(address);
              l.add(url);
            }
          } catch (UnknownHostException e) {
            throw new IllegalStateException("Could not resolve IP address for " + s, e);
          }
          return l.stream();
        }).collect(Collectors.toList()));
    } else {
      try {
        final LdapURL parsedUrl = new LdapURL(urls);
        urlSet.populate(Stream.of(InetAddress.getAllByName(parsedUrl.getHostname()))
          .map(ip -> {
            final LdapURL url = LdapURL.copy(parsedUrl);
            url.setRetryMetadata(new LdapURLRetryMetadata(this));
            url.setIpAddress(ip);
            return url;
          }).collect(Collectors.toList()));
      } catch (UnknownHostException e) {
        throw new IllegalStateException("Could not resolve IP address for " + urls, e);
      }
    }
  }
}
