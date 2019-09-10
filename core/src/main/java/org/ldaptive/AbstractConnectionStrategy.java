/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
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

  /** Duration of time to test inactive connections. */
  private Duration inactivePeriod = Duration.ofMinutes(1);

  /** Condition used to determine whether to attempt to activate an inactive URL. */
  private Predicate<LdapURL> inactiveCondition = url -> Instant.now().isAfter(
      url.getRetryMetadata().getFailureTime().plus(inactivePeriod));


  @Override
  public void populate(final String urls, final LdapURLSet urlSet)
  {
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


  @Override
  public Predicate<LdapURL> getInactiveCondition()
  {
    return inactiveCondition;
  }


  /**
   * Sets the inactive condition.
   *
   * @param  condition  that determines whether to test a connection
   */
  public void setInactiveCondition(final Predicate<LdapURL> condition)
  {
    inactiveCondition = condition;
  }
}
