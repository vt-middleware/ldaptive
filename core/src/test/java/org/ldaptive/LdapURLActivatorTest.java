/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapURLActivator}.
 *
 * @author  Middleware Services
 */
public class LdapURLActivatorTest
{


  /**
   * Unit test for {@link LdapURLActivator#testInactiveUrls()}.
   */
  @Test
  public void retryInactiveUrls()
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.setRetryCondition(url -> Instant.now().isAfter(url.getRetryMetadata().getFailureTime()));
    strategy.initialize("ldap://directory.ldaptive.org", url -> true);

    final LdapURLActivator activator = LdapURLActivator.getInstance();
    Assert.assertEquals(activator.getInactiveUrls().size(), 0);
    activator.testInactiveUrls();
    Assert.assertEquals(activator.getInactiveUrls().size(), 0);

    final LdapURL url = strategy.iterator().next();
    strategy.failure(url);
    Assert.assertEquals(activator.getInactiveUrls().size(), 1);
    activator.testInactiveUrls();
    Assert.assertEquals(activator.getInactiveUrls().size(), 0);
    activator.testInactiveUrls();
    Assert.assertEquals(activator.getInactiveUrls().size(), 0);
  }
}
