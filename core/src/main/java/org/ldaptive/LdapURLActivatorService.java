/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton which manages a single thread that periodically tests inactive LDAP URLs.
 *
 * @author  Middleware Services
 */
public final class LdapURLActivatorService
{

  /** Ldap activator period system property. */
  private static final String ACTIVATOR_PERIOD_PROPERTY = "org.ldaptive.urlActivatorPeriod";

  /** Ldap activator stale period system property. */
  private static final String STALE_PERIOD_PROPERTY = "org.ldaptive.urlActivatorStalePeriod";

  /** How often to test inactive connections. Default is 5 minutes. */
  private static final Duration ACTIVATOR_PERIOD = Duration.ofMinutes(
    LdapUtils.parseLong(System.getProperty(ACTIVATOR_PERIOD_PROPERTY, "5"), l -> l > 0, 5));

  /** Length of time to consider inactive connections stale. Default is 4 hours. */
  private static final Duration STALE_PERIOD = Duration.ofHours(
    LdapUtils.parseLong(System.getProperty(STALE_PERIOD_PROPERTY, "4"), l -> l > 0, 4));

  /** Instance of this singleton. */
  private static final LdapURLActivatorService INSTANCE = new LdapURLActivatorService();

  /** List of inactive URLs to test. */
  private final Queue<LdapURL> inactiveUrls = new ConcurrentLinkedQueue<>();


  /** Default constructor. */
  private LdapURLActivatorService()
  {
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
      1,
      r -> {
        final Thread t = new Thread(r, "ldaptive-ldap-url-activator");
        t.setDaemon(true);
        return t;
      });
    executor.scheduleAtFixedRate(
      this::testInactiveUrls,
      ACTIVATOR_PERIOD.toMillis(),
      ACTIVATOR_PERIOD.toMillis(),
      TimeUnit.MILLISECONDS);
  }


  /**
   * Returns the instance of this singleton.
   *
   * @return  LDAP URL activator service
   */
  public static LdapURLActivatorService getInstance()
  {
    return INSTANCE;
  }


  /**
   * Returns the activator period.
   *
   * @return  activator period
   */
  public static Duration getPeriod()
  {
    return ACTIVATOR_PERIOD;
  }


  /**
   * Registers an LDAP URL to be tested for activation. Once a URL becomes active it is automatically removed.
   *
   * @param  url  that is inactive and should be tested to become active
   */
  public void registerUrl(final LdapURL url)
  {
    inactiveUrls.add(LdapUtils.assertNotNullArg(url, "URL cannot be null"));
  }


  /**
   * Returns the collection of inactive urls.
   *
   * @return  inactive urls
   */
  public Collection<LdapURL> getInactiveUrls()
  {
    return Collections.unmodifiableCollection(inactiveUrls);
  }


  /**
   * Tests each registered URL. Removes URLs that successfully activated.
   */
  void testInactiveUrls()
  {
    for (LdapURL url : inactiveUrls) {
      if (!url.isActive() && url.getRetryMetadata().getConnectionStrategy().getRetryCondition().test(url)) {
        // note that the activate condition may block
        if (url.getRetryMetadata().getConnectionStrategy().getActivateCondition().test(url)) {
          url.getRetryMetadata().getConnectionStrategy().success(url);
        } else {
          url.getRetryMetadata().recordFailure(Instant.now());
          activateIfStale(url);
        }
      }
    }
    inactiveUrls.removeIf(LdapURL::isActive);
  }


  /**
   * Inspects the supplied url to determine how long it has been awaiting activation. For urls that have been attempting
   * activation for a long period, activate them to potentially free the memory. URLs will return to this service if
   * they are still in use and failing. See {@link #STALE_PERIOD}
   *
   * @param  url  to inspect
   */
  private void activateIfStale(final LdapURL url)
  {
    final Instant now = Instant.now();
    if (url.getRetryMetadata().getSuccessTime() == null) {
      if (url.getRetryMetadata().getCreateTime().plus(STALE_PERIOD).isBefore(now)) {
        // URL stale period has elapsed, activate as it may no longer be in use
        url.activate();
      }
    } else if (url.getRetryMetadata().getSuccessTime().plus(STALE_PERIOD).isBefore(now)) {
      // URL stale period has elapsed, activate as it may no longer be in use
      url.activate();
    }
  }


  /**
   * Removes all registered inactive URLs.
   */
  void clear()
  {
    inactiveUrls.clear();
  }
}
