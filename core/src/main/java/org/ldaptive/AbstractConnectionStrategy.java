/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.Map;
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

  /** Duration of time to test inactive connections. */
  private Duration inactivePeriod = Duration.ofMinutes(1);

  /** Condition used to determine whether to test an inactive URL. */
  private Predicate<RetryMetadata> inactiveCondition = metadata -> true;


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
  public synchronized void initialize(final String urls)
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
    ldapURLSet.activate(url);
  }


  @Override
  public void failure(final LdapURL url)
  {
    ldapURLSet.inactivate(url);
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
}
