/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Connection strategy that tries all IP addresses resolved from DNS. The order of IP addressees returned can be
 * controlled via the java.net.preferIPv4Stack or java.net.preferIPv6Addresses system property flags. This strategy
 * operates in an active/passive fashion.
 *
 * @author  Middleware Services
 */
public class DnsResolverConnectionStrategy extends AbstractConnectionStrategy
{

  /** Default time to live for DNS results. */
  protected static final Duration DEFAULT_TTL = Duration.ofHours(6);

  /** Custom iterator function. */
  private final Function<List<LdapURL>, Iterator<LdapURL>> iterFunction;

  /** Time to live for DNS records. */
  private final Duration dnsTtl;

  /** Name resolver function. */
  private Function<String, InetAddress[]> resolverFunction = name -> {
    try {
      return InetAddress.getAllByName(name);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Could not resolve IP address for " + name, e);
    }
  };

  /** LDAP URL string used to initialize this strategy. */
  private String ldapUrls;

  /** DNS expiration time. */
  private Instant expirationTime;


  /** Default constructor. */
  public DnsResolverConnectionStrategy()
  {
    this(DEFAULT_TTL);
  }


  /**
   * Creates a new DNS resolver connection strategy.
   *
   * @param  ttl  time to live for DNS records
   */
  public DnsResolverConnectionStrategy(final Duration ttl)
  {
    this(null, ttl);
  }


  /**
   * Creates a new DNS connection strategy.
   *
   * @param  function  that produces a custom iterator
   */
  public DnsResolverConnectionStrategy(final Function<List<LdapURL>, Iterator<LdapURL>> function)
  {
    this(function, DEFAULT_TTL);
  }


  /**
   * Creates a new DNS resolver connection strategy.
   *
   * @param  function  that produces a custom iterator
   * @param  ttl  time to live for DNS records
   */
  public DnsResolverConnectionStrategy(final Function<List<LdapURL>, Iterator<LdapURL>> function, final Duration ttl)
  {
    iterFunction = function;
    dnsTtl = ttl;
  }


  /**
   * Returns the name resolution function.
   *
   * @return  name resolution function
   */
  public final Function<String, InetAddress[]> getResolverFunction()
  {
    return resolverFunction;
  }


  /**
   * Sets the function used to resolve names.
   *
   * @param  func  to set
   */
  public final void setResolverFunction(final Function<String, InetAddress[]> func)
  {
    checkImmutable();
    resolverFunction = func;
  }


  @Override
  public Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    if (Instant.now().isAfter(expirationTime)) {
      populate(ldapUrls, ldapURLSet);
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
    ldapUrls = urls;
    if (urls.contains(" ")) {
      urlSet.populate(Stream.of(urls.split(" "))
        .flatMap(s -> {
          final List<LdapURL> l = new ArrayList<>(2);
          final LdapURL parsedUrl = new LdapURL(s);
          for (InetAddress address : resolverFunction.apply(parsedUrl.getHostname())) {
            final LdapURL url = LdapURL.copy(parsedUrl);
            url.setRetryMetadata(new LdapURLRetryMetadata(this));
            url.setInetAddress(address);
            l.add(url);
          }
          return l.stream();
        }).collect(Collectors.toList()));
    } else {
      final LdapURL parsedUrl = new LdapURL(urls);
      urlSet.populate(Stream.of(resolverFunction.apply(parsedUrl.getHostname()))
        .map(ip -> {
          final LdapURL url = LdapURL.copy(parsedUrl);
          url.setRetryMetadata(new LdapURLRetryMetadata(this));
          url.setInetAddress(ip);
          return url;
        }).collect(Collectors.toList()));
    }
    expirationTime = Instant.now().plus(dnsTtl);
  }


  @Override
  public DnsResolverConnectionStrategy newInstance()
  {
    final DnsResolverConnectionStrategy strategy = new DnsResolverConnectionStrategy(iterFunction, dnsTtl);
    strategy.setResolverFunction(resolverFunction);
    strategy.setRetryCondition(getRetryCondition());
    return strategy;
  }
}
