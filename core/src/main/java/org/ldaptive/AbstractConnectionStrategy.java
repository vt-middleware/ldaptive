/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connection strategy implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConnectionStrategy implements ConnectionStrategy
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Set of LDAP URLs to attempt connections to. */
  protected LdapURLSet ldapURLSet;

  /** Whether this strategy has been successfully initialized. */
  private boolean initialized;

  /** Condition used to determine whether to activate a URL. */
  private Predicate<LdapURL> activateCondition;

  /** Duration of time to test inactive connections. */
  private Duration inactivePeriod = Duration.ofMinutes(1);

  /** Condition used to determine whether to test an inactive URL. */
  private Predicate<LdapURL> retryCondition = url -> Instant.now().isAfter(
    url.getRetryMetadata().getFailureTime().plus(inactivePeriod));

  /** Executor for testing inactive URLs. */
  private ScheduledExecutorService executor;


  @Override
  public boolean isInitialized()
  {
    return initialized;
  }


  @Override
  public synchronized void initialize(final String urls, final Predicate<LdapURL> condition)
  {
    if (isInitialized()) {
      throw new IllegalStateException("Strategy has already been initialized");
    }
    ldapURLSet = new LdapURLSet(this, urls);
    activateCondition = condition;
    initialized = true;
  }


  @Override
  public void populate(final String urls, final LdapURLSet urlSet)
  {
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("urls cannot be empty or null");
    }
    if (urls.contains(" ")) {
      urlSet.populate(Stream.of(urls.split(" ")).map(LdapURL::new).collect(Collectors.toList()));
    } else {
      urlSet.populate(Collections.singletonList(new LdapURL(urls)));
    }
  }


  @Override
  public Duration getInactivePeriod()
  {
    return inactivePeriod;
  }


  /**
   * Sets the inactive period.
   *
   * @param  period  inactive period
   */
  public void setInactivePeriod(final Duration period)
  {
    inactivePeriod = period;
  }


  /**
   * Returns the retry condition which determines whether an attempt should be made to active a URL.
   *
   * @return  retry condition
   */
  public Predicate<LdapURL> getRetryCondition()
  {
    return retryCondition;
  }


  /**
   * Sets the retry condition which determines whether an attempt should be made to activate a URL.
   *
   * @param  condition  that determines whether to active a URL
   */
  public void setRetryCondition(final Predicate<LdapURL> condition)
  {
    retryCondition = condition;
  }


  @Override
  public void success(final LdapURL url)
  {
    url.activate();
  }


  @Override
  public void failure(final LdapURL url)
  {
    url.deactivate();
    createInactiveExecutor();
  }


  /**
   * Creates a new scheduled thread executor if one is not already running. Schedules a task which tests inactive URLs.
   * The task shuts down the executor if there are no inactive URLs to test.
   */
  private void createInactiveExecutor()
  {
    synchronized (this) {
      if (executor == null && !ldapURLSet.getInactiveUrls().isEmpty()) {
        executor = Executors.newScheduledThreadPool(
          ldapURLSet.size() + 1,
          r -> {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
          });

        executor.scheduleAtFixedRate(
          () -> {
            synchronized (this) {
              if (ldapURLSet.getInactiveUrls().isEmpty()) {
                try {
                  executor.shutdown();
                } finally {
                  executor = null;
                }
                return;
              }
              for (LdapURL url : ldapURLSet.getInactiveUrls()) {
                if (retryCondition.test(url)) {
                  executor.submit(() -> {
                    if (activateCondition.test(url)) {
                      success(url);
                    } else {
                      url.getRetryMetadata().recordFailure(Instant.now());
                    }
                  });
                }
              }
            }
          },
          getInactivePeriod().toMillis(),
          getInactivePeriod().toMillis(),
          TimeUnit.MILLISECONDS);
      }
    }
  }
}
