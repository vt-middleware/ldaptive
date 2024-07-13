/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapURLActivatorService}.
 *
 * @author  Middleware Services
 */
public class LdapURLActivatorServiceTest
{


  /**
   * Unit test for {@link LdapURLActivatorService#testInactiveUrls()}.
   */
  @Test
  public void retryInactiveUrls()
    throws Exception
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.setRetryCondition(url -> Instant.now().isAfter(url.getRetryMetadata().getFailureTime()));
    strategy.initialize("ldap://directory.ldaptive.org", url -> true);

    final LdapURLActivatorService activator = LdapURLActivatorService.getInstance();
    activator.clear();
    assertThat(activator.getInactiveUrls()).isEmpty();
    activator.testInactiveUrls();
    assertThat(activator.getInactiveUrls()).isEmpty();

    final LdapURL url = strategy.iterator().next();
    strategy.failure(url);
    assertThat(activator.getInactiveUrls()).hasSize(1);
    // sleep here to guarantee the retry condition succeeds
    Thread.sleep(1);
    activator.testInactiveUrls();
    assertThat(activator.getInactiveUrls()).isEmpty();
    activator.testInactiveUrls();
    assertThat(activator.getInactiveUrls()).isEmpty();
  }
}
