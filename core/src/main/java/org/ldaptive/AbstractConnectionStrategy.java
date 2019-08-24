/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
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

  /** Active LDAP URLs. */
  protected Map<Integer, LdapURL> active;

  /** Inactive LDAP URLs. */
  protected Map<RetryMetadata, Map.Entry<Integer, LdapURL>> inactive = new LinkedHashMap<>();

  /** Lock for active and inactive maps. */
  protected Object lock = new Object();

  /** Whether this strategy has been successfully initialized. */
  protected boolean initialized;

  /** Duration of time to test inactive connections. */
  private Duration inactivePeriod = Duration.ofMinutes(1);

  /** Condition used to determine whether to test an inactive URL. */
  private Predicate<RetryMetadata> inactiveCondition = metadata -> true;


  @Override
  public boolean isInitialized()
  {
    return initialized;
  }


  @Override
  public synchronized void initialize(final String urls)
  {
    if (isInitialized()) {
      throw new IllegalStateException("Strategy has already been initialized");
    }
    if (urls == null || urls.isEmpty()) {
      throw new IllegalArgumentException("urls cannot be empty or null");
    }
    if (urls.contains(" ")) {
      int i = 1;
      for (String url : urls.split(" ")) {
        active.put(i++, new LdapURL(url));
      }
    } else {
      active.put(1, new LdapURL(urls));
    }
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
  public Predicate<RetryMetadata> getInactiveCondition()
  {
    return inactiveCondition;
  }


  /**
   * Sets the inactive condition.
   *
   * @param  condition  that determines whether to test a connection
   */
  public void setInactiveCondition(final Predicate<RetryMetadata> condition)
  {
    inactiveCondition = condition;
  }


  @Override
  public void success(final LdapURL url)
  {
    synchronized (lock) {
      final Optional<Map.Entry<Integer, LdapURL>> entry = getInactive(url);
      if (entry.isPresent()) {
        inactive.entrySet().removeIf(e -> e.getValue().equals(entry.get()));
        active.put(entry.get().getKey(), entry.get().getValue());
      }
    }
  }


  @Override
  public void failure(final LdapURL url)
  {
    synchronized (lock) {
      final Optional<Map.Entry<Integer, LdapURL>> entry = getActive(url);
      if (entry.isPresent()) {
        active.entrySet().removeIf(e -> e.equals(entry.get()));
        inactive.put(new RetryMetadata(), entry.get());
      }
    }
  }


  @Override
  public Map<Integer, LdapURL> active()
  {
    synchronized (lock) {
      return Collections.unmodifiableMap(active);
    }
  }


  @Override
  public Map<RetryMetadata, Map.Entry<Integer, LdapURL>> inactive()
  {
    synchronized (lock) {
      return Collections.unmodifiableMap(inactive);
    }
  }


  /**
   * Returns the active entry for the supplied url.
   *
   * @param  url  to find
   *
   * @return  active entry or null
   */
  protected Optional<Map.Entry<Integer, LdapURL>> getActive(final LdapURL url)
  {
    return active.entrySet().stream().filter(e -> url.equals(e.getValue())).findAny();
  }


  /**
   * Returns the inactive entry for the supplied url.
   *
   * @param  url  to find
   *
   * @return  inactive entry or null
   */
  protected Optional<Map.Entry<Integer, LdapURL>> getInactive(final LdapURL url)
  {
    return inactive.entrySet().stream()
      .filter(e -> url.equals(e.getValue().getValue())).map(e -> e.getValue()).findAny();
  }
}
