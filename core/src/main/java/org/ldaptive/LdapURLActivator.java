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
 * Singleton which manages a single thread that periodically tests a list of LDAP URLs.
 *
 * @author  Middleware Services
 */
public final class LdapURLActivator
{

  /** How often to test inactive connections. */
  public static final Duration PERIOD = Duration.ofMinutes(5);

  /** Instance of this singleton. */
  private static final LdapURLActivator INSTANCE = new LdapURLActivator();

  /** List of inactive URLs to test. */
  private final List<LdapURL> inactiveUrls = new ArrayList<>();

  /** Executor for testing inactive URLs. */
  private final ScheduledThreadPoolExecutor executor;


  /** Default constructor. */
  private LdapURLActivator()
  {
    executor = new ScheduledThreadPoolExecutor(
      1,
      r -> {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
      });
    executor.scheduleAtFixedRate(
      () -> {
        testInactiveUrls();
      },
      PERIOD.toMillis(),
      PERIOD.toMillis(),
      TimeUnit.MILLISECONDS);
  }


  /**
   * Returns the instance of this singleton.
   *
   * @return  connection strategy retry
   */
  public static LdapURLActivator getInstance()
  {
    return INSTANCE;
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
    inactiveUrls.removeIf(url -> url.isActive());
  }
}
