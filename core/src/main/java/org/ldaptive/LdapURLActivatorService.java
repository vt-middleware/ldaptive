/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  /** How often to test inactive connections. */
  private static final Duration ACTIVATOR_PERIOD = Duration.ofMinutes(
    Long.parseLong(System.getProperty(ACTIVATOR_PERIOD_PROPERTY, "5")));

  /** Instance of this singleton. */
  private static final LdapURLActivatorService INSTANCE = new LdapURLActivatorService();

  /** List of inactive URLs to test. */
  private final List<LdapURL> inactiveUrls = new ArrayList<>();

  /** Executor for testing inactive URLs. */
  private final ScheduledThreadPoolExecutor executor;


  /** Default constructor. */
  private LdapURLActivatorService()
  {
    executor = new ScheduledThreadPoolExecutor(
      1,
      r -> {
        final Thread t = new Thread(r, getClass().getSimpleName() + "-" + hashCode());
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
    inactiveUrls.add(url);
  }


  /**
   * Returns the list of inactive urls.
   *
   * @return  inactive urls
   */
  public List<LdapURL> getInactiveUrls()
  {
    return Collections.unmodifiableList(inactiveUrls);
  }


  /**
   * Tests each registered URL. Removes URLs that successfully activated.
   */
  protected void testInactiveUrls()
  {
    for (LdapURL url : inactiveUrls) {
      if (!url.isActive() && url.getRetryMetadata().getConnectionStrategy().getRetryCondition().test(url)) {
        // note that the activate condition may block
        if (url.getRetryMetadata().getConnectionStrategy().getActivateCondition().test(url)) {
          url.getRetryMetadata().getConnectionStrategy().success(url);
        } else {
          url.getRetryMetadata().recordFailure(Instant.now());
        }
      }
    }
    inactiveUrls.removeIf(LdapURL::isActive);
  }


  /**
   * Removes all registered inactive URLs.
   */
  void clear()
  {
    inactiveUrls.clear();
  }
}
