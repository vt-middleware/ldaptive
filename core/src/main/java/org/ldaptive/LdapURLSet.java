/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of LDAP URLs with helper functions for common connection strategies.
 *
 * @author  Middleware Services
 */
public class LdapURLSet
{
  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** List of LDAP URLs to connect to in order provided by the connection strategy. */
  protected final List<LdapURL> urls = new ArrayList<>();

  /** Lock for active and inactive maps. */
  private final Object lock = new Object();

  /** Strategy responsible for producing LDAP URLs and next URL to try. */
  private final ConnectionStrategy connectionStrategy;

  /** Usage counter. */
  private final AtomicInteger counter = new AtomicInteger();


  /**
   * Creates a new instance.
   *
   * @param strategy Connection strategy.
   * @param ldapUrls Space-delimited string of URLs describing the LDAP hosts to connect to. The URLs in the string
   *                 are commonly {@code ldap://} or {@code ldaps://} URLs that directly describe the hosts to connect
   *                 to, but may also describe a resource from which to obtain LDAP connection URLs as is the case for
   *                 {@link DnsSrvConnectionStrategy} that use URLs with the scheme {@code dns:}.
   */
  public LdapURLSet(final ConnectionStrategy strategy, final String ldapUrls)
  {
    connectionStrategy = strategy;
    connectionStrategy.populate(ldapUrls, this);
  }


  /**
   * @return Usage count, i.e. number of calls to {@link #doWithNextActiveUrl(Consumer)}.
   */
  public int getUsageCount()
  {
    return counter.get();
  }


  /**
   * @return True if there are any active LDAP URLs in the set, false otherwise.
   */
  public boolean hasActiveUrls()
  {
    return urls.stream().anyMatch(LdapURL::isActive);
  }

  /**
   * @return List of active URLs in order they were added.
   */
  public List<LdapURL> getActiveUrls()
  {
    return urls.stream().filter(LdapURL::isActive).collect(Collectors.toList());
  }

  /**
   * Executes the given consumer on the next active URL and increments the usage counter.
   *
   * @param consumer LDAP URL consumer.
   *
   * @return The active LDAP URL that was consumed.
   *
   * @throws IllegalStateException If no active URLs are available.
   * @throws LdapException When the consumer function throws.
   */
  public LdapURL doWithNextActiveUrl(final Consumer<LdapURL> consumer) throws LdapException
  {
    final LdapURL url;
    synchronized (lock) {
      url = connectionStrategy.next(this);
      counter.incrementAndGet();
    }
    try {
      logger.trace("Sending {} to consumer {}", url.getHostnameWithSchemeAndPort(), consumer);
      consumer.accept(url);
    } catch (RuntimeException e) {
      url.deactivate();
      if (e.getCause() instanceof LdapException) {
        throw (LdapException) e.getCause();
      }
      throw new LdapException("Error consuming " + url.getHostnameWithSchemeAndPort(), e);
    }
    return url;
  }


  /**
   * @return True if there are any inactive LDAP URLs in the set, false otherwise.
   */
  public boolean hasInactiveUrls()
  {
    return urls.stream().anyMatch(u -> !u.isActive());
  }


  /**
   * @return List of inactive URLs in order they were added.
   */
  public List<LdapURL> getInactiveUrls()
  {
    return urls.stream().filter(u -> !u.isActive()).collect(Collectors.toList());
  }


  /**
   * Executes the given predicate on the next inactive URL such that if the predicate evaluates to true, the URL
   * is activated. If there are no inactive URLs, this is a no-op.
   *
   * @param predicate Predicate that returns true on success, false otherwise.
   *
   * @return The inactive LDAP URL that was tested, or null if there were no inactive URLs to test.
   */
  public LdapURL doWithNextInactiveUrl(final Predicate<LdapURL> predicate)
  {
    final LdapURL url = urls.stream()
        .filter(u -> !u.isActive())
        .findFirst()
        .orElse(null);
    if (url != null && predicate.test(url)) {
      url.activate();
    }
    return url;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[")
        .append(getClass().getName()).append("@").append(hashCode()).append("::")
        .append("active=").append(getActiveUrls()).append(", ")
        .append("inactive=").append(getInactiveUrls()).toString();
  }


  /**
   * Populates this set with a list of URLs in the order produced by
   * {@link ConnectionStrategy#populate(String, LdapURLSet)}. This method MUST be called before the set is used, but
   * MAY be called subsequently periodically to refresh the set of LDAP URLs.
   *
   * @param ldapUrls List of LDAP URLs to add to this set.
   */
  protected synchronized void populate(final List<LdapURL> ldapUrls)
  {
    // Copy activity state from any URLs currently in the set that match new entries
    for (LdapURL url : urls) {
      final LdapURL match = ldapUrls.stream().filter(u -> u.equals(url)).findFirst().orElse(null);
      if (match != null && !url.isActive()) {
        match.deactivate();
      }
    }
    this.urls.clear();
    this.urls.addAll(ldapUrls);
  }
}
