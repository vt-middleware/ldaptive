/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Duration;
import java.util.function.Predicate;
import org.ldaptive.RetryMetadata;

/**
 * Reconnect strategy for testing.
 *
 * @author  Middleware Services
 */
public class BackoffAutoReconnect implements Predicate<RetryMetadata>
{

  @Override
  public boolean test(final RetryMetadata metadata)
  {
    if (metadata.getAttempts() > 0) {
      try {
        Thread.sleep(Duration.ofSeconds(2).multipliedBy(metadata.getAttempts()).toMillis());
      } catch (InterruptedException e) {}
    }
    return true;
  }
}
