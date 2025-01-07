/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapURLActivatorService}.
 *
 * @author  Middleware Services
 */
public class LdapURLActivatorServiceTest
{


  @AfterMethod
  public void clearActivator()
  {
    LdapURLActivatorService.getInstance().clear();
    assertThat(LdapURLActivatorService.getInstance().getInactiveUrls()).isEmpty();
  }


  /**
   * Unit test for {@link LdapURLActivatorService#testInactiveUrls()}.
   */
  @Test(priority = 1)
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


  /**
   * Unit test for {@link LdapURLActivatorService#registerUrl(LdapURL)} thread safety.
   */
  @Test(priority = 2)
  public void threadSafeRegisterUrl()
    throws Exception
  {
    final Thread t1 = new Thread(() -> {
      for (int i = 0; i < 5000; i++) {
        final LdapURL url = new LdapURL("ldap://ds" + i + ".ldaptive.org");
        LdapURLActivatorService.getInstance().registerUrl(url);
      }
    });

    final Thread t2 = new Thread(() -> {
      for (int i = 5000; i < 10000; i++) {
        final LdapURL url = new LdapURL("ldap://ds" + i + ".ldaptive.org");
        LdapURLActivatorService.getInstance().registerUrl(url);
      }
    });

    t1.start();
    t2.start();

    try {
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    assertThat(LdapURLActivatorService.getInstance().getInactiveUrls()).hasSize(10000);
  }


  /**
   * Unit test for {@link LdapURLActivatorService#testInactiveUrls()} thread safety.
   */
  @Test(priority = 3)
  public void threadSafeInactiveUrls()
    throws Exception
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.setRetryCondition(ldapURL -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return true;
    });
    strategy.initialize("ldap://ds.ldaptive.org", url -> true);

    final LdapURL url = new LdapURL("ldap://ds.ldaptive.org");
    url.deactivate();
    url.setRetryMetadata(new LdapURLRetryMetadata(strategy));
    LdapURLActivatorService.getInstance().registerUrl(url);

    final Thread t1 = new Thread(() -> LdapURLActivatorService.getInstance().testInactiveUrls());

    final AtomicInteger count = new AtomicInteger(0);
    final Thread t2 = new Thread(() -> {
      for (int i = 0; i < 50; i++) {
        LdapURLActivatorService.getInstance().registerUrl(
          new LdapURL("ldap://ds" + count.incrementAndGet() + ".ldaptive.org"));
      }
    });

    t1.start();
    Thread.sleep(50);
    t2.start();
    try {
      t1.join();
      t2.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Unit test for {@link LdapURLActivatorService#testInactiveUrls()} thread safety.
   */
  @Test(priority = 4)
  public void threadSafeInactiveUrlsRandomWait()
    throws Exception
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.setRetryCondition(ldapURL -> {
      final int random = new Random().nextInt(100) + 1;
      try {
        Thread.sleep(random);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return random % 2 == 0;
    });
    strategy.initialize("ldap://ds.ldaptive.org", url -> true);

    final AtomicInteger count = new AtomicInteger(0);
    for (int i = 0; i < 50; i++) {
      final Thread t1 = new Thread(() -> {
        final LdapURL url = new LdapURL("ldap://ds" + count.incrementAndGet() + ".ldaptive.org");
        url.deactivate();
        url.setRetryMetadata(new LdapURLRetryMetadata(strategy));
        LdapURLActivatorService.getInstance().registerUrl(url);
      });

      final Thread t2 = new Thread(() -> LdapURLActivatorService.getInstance().testInactiveUrls());

      t1.start();
      t2.start();
      try {
        t1.join();
        t2.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }


  /**
   * Unit test for {@link LdapURLActivatorService} activating stale URLs.
   */
  @Test(priority = 5)
  public void activateStaleUrlCreateTime()
    throws Exception
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.setRetryCondition(ldapURL -> true);
    strategy.initialize("ldap://ds.ldaptive.org", url -> false);

    final LdapURL url1 = new LdapURL("ldap://ds1.ldaptive.org");
    url1.deactivate();
    url1.setRetryMetadata(new LdapURLRetryMetadata(strategy));
    LdapURLActivatorService.getInstance().registerUrl(url1);

    final LdapURL url2 = new LdapURL("ldap://ds2.ldaptive.org");
    url2.deactivate();
    final Clock clock = Clock.fixed(Instant.now().minus(Duration.ofHours(4)), ZoneId.systemDefault());
    url2.setRetryMetadata(new LdapURLRetryMetadata(clock, strategy));
    LdapURLActivatorService.getInstance().registerUrl(url2);

    final LdapURLActivatorService activator = LdapURLActivatorService.getInstance();
    assertThat(activator.getInactiveUrls()).containsExactlyInAnyOrder(url1, url2);
    LdapURLActivatorService.getInstance().testInactiveUrls();
    assertThat(activator.getInactiveUrls()).containsExactly(url1);
  }


  /**
   * Unit test for {@link LdapURLActivatorService} activating stale URLs.
   */
  @Test(priority = 6)
  public void activateStaleUrlSuccessTime()
    throws Exception
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.setRetryCondition(ldapURL -> true);
    strategy.initialize("ldap://ds.ldaptive.org", url -> false);

    final LdapURL url1 = new LdapURL("ldap://ds1.ldaptive.org");
    final LdapURLRetryMetadata retryMetadata1 = new LdapURLRetryMetadata(strategy);
    retryMetadata1.recordSuccess(Instant.now());
    url1.setRetryMetadata(retryMetadata1);
    url1.deactivate();
    LdapURLActivatorService.getInstance().registerUrl(url1);

    final LdapURL url2 = new LdapURL("ldap://ds2.ldaptive.org");
    final LdapURLRetryMetadata retryMetadata2 = new LdapURLRetryMetadata(strategy);
    retryMetadata2.recordSuccess(
      Instant.now(Clock.fixed(Instant.now().minus(Duration.ofHours(4)), ZoneId.systemDefault())));
    url2.setRetryMetadata(retryMetadata2);
    url2.deactivate();
    LdapURLActivatorService.getInstance().registerUrl(url2);

    final LdapURLActivatorService activator = LdapURLActivatorService.getInstance();
    assertThat(activator.getInactiveUrls()).containsExactlyInAnyOrder(url1, url2);
    LdapURLActivatorService.getInstance().testInactiveUrls();
    assertThat(activator.getInactiveUrls()).containsExactly(url1);
  }
}
