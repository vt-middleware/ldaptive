/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.ldaptive.handler.ResultPredicate;

/**
 * Base class for profiling.
 *
 * @author  Middleware Services
 */
public abstract class AbstractProfile
{

  /** Number to start with for UID creation. */
  protected static final int UID_START = 1001;

  /** Number of operations performed. */
  private static final AtomicInteger COUNT = new AtomicInteger();


  /**
   * Main method for executing a profile.
   *
   * @param  args  command line arguments
   *
   * @throws  Exception  on an unexpected error
   */
  // CheckStyle:MagicNumber OFF
  public static void main(final String[] args)
    throws Exception
  {
    final String clazz = args[0];
    final String host = args[1];
    final int port = Integer.parseInt(args[2]);
    final int threadCount = Integer.parseInt(args[3]);
    final int threadSleep = Integer.parseInt(args[4]);
    final int iterations = Integer.parseInt(args[5]);

    final AbstractProfile test = createInstance(Class.forName(clazz));
    test.setBaseDn(System.getProperty("ldapBaseDn"));
    test.setBindDn(System.getProperty("ldapBindDn"));
    test.setBindCredential(System.getProperty("ldapBindCredential"));
    test.initialize(host, port);
    test.createEntries(100);

    System.out.println();
    System.out.println("##############################");
    System.out.println("# Start profile for " + test);
    System.out.println("#   threadCount: " + threadCount);
    System.out.println("#   threadSleep: " + threadSleep);
    System.out.println("#   iterations: " + iterations);
    System.out.println("##############################");
    System.out.println();
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    if (iterations == -1) {
      final ScheduledExecutorService counter = Executors.newSingleThreadScheduledExecutor();
      counter.scheduleAtFixedRate(
        () -> System.out.println("  " + LocalDateTime.now() + ": received " + COUNT + " results "),
        10,
        10,
        TimeUnit.SECONDS);
      final Random r = new Random();
      while (true) {
        final int uid = r.nextInt(100) + UID_START;
        executor.submit(() -> test.doOperation(
          o -> {
            if (o == null) {
              System.out.println("RECEIVED NULL RESULT");
            } else {
              COUNT.getAndIncrement();
            }
          },
          uid));
        if (threadSleep > 0) {
          Thread.sleep(threadSleep);
        }
      }
    } else {
      final List<Callable<Void>> callables = new ArrayList<>(iterations);
      final CountDownLatch latch = new CountDownLatch(iterations);
      final Random r = new Random();
      for (int i = 0; i < iterations; i++) {
        final int uid = r.nextInt(100) + 1001;
        callables.add(() -> {
          test.doOperation(
            o -> {
              if (o == null) {
                System.out.println("RECEIVED NULL RESULT");
              } else if (o instanceof Exception) {
                System.out.println("RECEIVED EXCEPTION:: " + ((Exception) o).getMessage());
              } else {
                COUNT.getAndIncrement();
              }
              latch.countDown();
            },
            uid);
          return null;
        });
      }
      long t = System.currentTimeMillis();
      executor.invokeAll(callables);
      latch.await();
      t = System.currentTimeMillis() - t;
      System.out.println("##############################");
      System.out.println("# End profile for " + test + " in " + t + "ms : " + COUNT + " results");
      System.out.println("##############################");
    }

    test.shutdown();
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
  }
  // CheckStyle:MagicNumber ON


  /**
   * Creates a concrete instance of the profile class.
   *
   * @param  clazz  profile class that will run
   *
   * @return  new instance of a profile class
   */
  private static AbstractProfile createInstance(final Class<?> clazz)
  {
    try {
      return (AbstractProfile) clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Could not create class", e);
    }
  }


  /**
   * Prepare this profile for use.
   *
   * @param  host  to connect to
   * @param  port  to connect to
   */
  protected abstract void initialize(String host, int port);


  /**
   * Creates entries needed for searching.
   *
   * @param  count  number of entries to create
   */
  protected abstract void createEntries(int count);


  /**
   * Sets a base DN.
   *
   * @param  dn  base DN
   */
  protected abstract void setBaseDn(String dn);


  /**
   * Sets a bind DN.
   *
   * @param  dn  bind DN
   */
  protected abstract void setBindDn(String dn);


  /**
   * Sets a bind DN password.
   *
   * @param  pass  bind DN password
   */
  protected abstract void setBindCredential(String pass);


  /**
   * Perform an LDAP operation.
   *
   * @param  consumer  to collect results
   * @param  uid  to perform operation on
   */
  protected abstract void doOperation(Consumer<Object> consumer, int uid);


  /**
   * Clean up any resources associated with this profile.
   */
  protected abstract void shutdown();


  /**
   * Create LDAP entries.
   *
   * @param  cf  connection factory to create entries with
   * @param  start  uid to start creation at
   * @param  count  number of entries to create
   */
  protected static void createEntries(final ConnectionFactory cf, final int start, final int count)
  {
    final AddOperation create = AddOperation.builder()
      .factory(cf)
      .throwIf(ResultPredicate.NOT_SUCCESS)
      .build();
    for (int i = start; i < start + count; i++) {
      try {
        create.execute(
          AddRequest.builder()
            .dn(String.format("uid=%s,ou=test,dc=vt,dc=edu", i))
            .attributes(
              LdapAttribute.builder().name("uid").values(Integer.toString(i)).build(),
              LdapAttribute.builder().name("cn").values("Test User").build(),
              LdapAttribute.builder().name("givenName").values("Test").build(),
              LdapAttribute.builder().name("sn").values("User").build(),
              LdapAttribute.builder()
                .name("objectClass").values("organizationalPerson", "person", "top", "inetOrgPerson").build(),
              LdapAttribute.builder().name("userPassword").values(String.format("password%s", i)).build())
            .build());
      } catch (LdapException e) {
        throw new IllegalStateException("Could not create entry", e);
      }
    }
  }
}
