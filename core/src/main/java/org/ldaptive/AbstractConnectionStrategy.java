/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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

  /** Whether this strategy has been successfully initialized. */
  protected boolean initialized;

  /** Set of LDAP URLs to attempt connections to. */
  protected final LdapURLSet ldapURLSet;

  /** Condition used to determine whether to activate a URL. */
  protected Predicate<LdapURL> activateCondition;

  /** Duration of time to test inactive connections. */
  private Duration inactivePeriod = Duration.ofMinutes(1);

  /** Condition used to determine whether to test an inactive URL. */
  private Predicate<RetryMetadata> retryCondition = metadata -> true;

  /** Executor for testing inactive URLs. */
  private ScheduledExecutorService executor;


  /**
   * Creates a new abstract connection strategy.
   *
   * @param  type  of LDAP URL set
   */
  public AbstractConnectionStrategy(final LdapURLSet.Type type)
  {
    ldapURLSet = new LdapURLSet(type);
  }


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
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("urls cannot be empty or null");
    }
    if (urls.contains(" ")) {
      ldapURLSet.add(Stream.of(urls.split(" ")).map(LdapURL::new).toArray(LdapURL[]::new));
    } else {
      ldapURLSet.add(new LdapURL(urls));
    }
    activateCondition = condition;
    initialized = true;
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


  @Override
  public Predicate<RetryMetadata> getRetryCondition()
  {
    return retryCondition;
  }


  /**
   * Sets the retry condition which determines whether an attempt should be made to activate a URL.
   *
   * @param  condition  that determines whether to active a URL
   */
  public void setRetryCondition(final Predicate<RetryMetadata> condition)
  {
    retryCondition = condition;
  }


  @Override
  public void success(final LdapURL url)
  {
    ldapURLSet.activate(url);
  }


  @Override
  public void failure(final LdapURL url)
  {
    ldapURLSet.inactivate(url);
    createInactiveExecutor();
  }


  @Override
  public Map<Integer, LdapURL> active()
  {
    return ldapURLSet.getActiveUrls();
  }


  @Override
  public Map<RetryMetadata, Map.Entry<Integer, LdapURL>> inactive()
  {
    return ldapURLSet.getInactiveUrls();
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
              for (Map.Entry<RetryMetadata, Map.Entry<Integer, LdapURL>> entry :
                ldapURLSet.getInactiveUrls().entrySet()) {
                // ensure inactive URL waits at least the inactive period before testing
                if (Instant.now().isAfter(entry.getKey().getFailureTime().plus(inactivePeriod))) {
                  if (retryCondition.test(entry.getKey())) {
                    executor.submit(() -> {
                      if (activateCondition.test(entry.getValue().getValue())) {
                        success(entry.getValue().getValue());
                      } else {
                        entry.getKey().incrementAttempts();
                      }
                    });
                  }
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
