/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.ldaptive.transport.netty.SingletonTransport;

/**
 * Base class for profiling.
 *
 * @author  Middleware Services
 */
public abstract class AbstractProfile
{

  /** Number to start with for UID creation. */
  protected static final int UID_START = 549801;

  /** Number of threads to execute searches. */
  protected static int threadCount;

  /** Time to sleep between search requests. */
  protected static int threadSleep;

  /** Number of searches to execute. */
  protected static int iterations;

  /** Number of searches requested. */
  private static final AtomicInteger REQUEST_COUNT = new AtomicInteger();

  /** Number of entries received. */
  private static final AtomicInteger ENTRY_COUNT = new AtomicInteger();

  /** Number of results received. */
  private static final AtomicInteger RESULT_COUNT = new AtomicInteger();


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
    threadCount = Integer.parseInt(args[3]);
    threadSleep = Integer.parseInt(args[4]);
    iterations = Integer.parseInt(args[5]);

    final AbstractProfile test = createInstance(Class.forName(clazz));
    test.setBaseDn(System.getProperty("ldapBaseDn"));
    test.setBindDn(System.getProperty("ldapBindDn"));
    test.setBindCredential(System.getProperty("ldapBindCredential"));
    test.initialize(host, port);

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
        () -> System.out.println(test.report()),
        10,
        10,
        TimeUnit.SECONDS);
      final Random r = new Random();
      while (true) {
        final int uid = r.nextInt(50) + UID_START;
        executor.submit(() -> test.doOperation(
          o -> {
            if (o instanceof Entry) {
              ENTRY_COUNT.getAndIncrement();
            } else if (o instanceof Result) {
              RESULT_COUNT.getAndIncrement();
            } else if (o instanceof Exception) {
              System.out.println("RECEIVED EXCEPTION:: " + ((Exception) o).getMessage());
            } else {
              System.out.println("RECEIVED UNEXPECTED TYPE: " + o);
            }
          },
          uid));
        REQUEST_COUNT.getAndIncrement();
        if (threadSleep > 0) {
          Thread.sleep(threadSleep);
        }
      }
    } else {
      final List<Callable<Void>> callables = new ArrayList<>(iterations);
      final CountDownLatch latch = new CountDownLatch(iterations);
      final Random r = new Random();
      for (int i = 0; i < iterations; i++) {
        final int uid = r.nextInt(50) + UID_START;
        callables.add(() -> {
          test.doOperation(
            o -> {
              if (o instanceof Entry) {
                ENTRY_COUNT.getAndIncrement();
              } else if (o instanceof Result) {
                RESULT_COUNT.getAndIncrement();
                latch.countDown();
              } else if (o instanceof Exception) {
                System.out.println("RECEIVED EXCEPTION:: " + ((Exception) o).getMessage());
                latch.countDown();
              } else {
                System.out.println("RECEIVED UNEXPECTED TYPE: " + o);
                latch.countDown();
              }
            },
            uid);
          REQUEST_COUNT.getAndIncrement();
          return null;
        });
      }
      long t = System.currentTimeMillis();
      executor.invokeAll(callables);
      latch.await();
      t = System.currentTimeMillis() - t;
      System.out.println("##############################");
      System.out.println(
        "# End profile for " + test + " in " + t + "ms : " +
          ENTRY_COUNT + "/" + RESULT_COUNT + "/" + REQUEST_COUNT + " results");
      System.out.println("##############################");
    }

    test.shutdown();
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
    SingletonTransport.shutdown();
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
   * Return periodic report string.
   *
   * @return  report string
   */
  protected String report()
  {
    return "  " + LocalDateTime.now() +
      " [" + countOpenConnections() + "] " +
      "received " + ENTRY_COUNT + "/" + RESULT_COUNT + "/" + REQUEST_COUNT + " results";
  }


  /**
   * Returns the number of open connections to the supplied host. Uses 'netstat -al' to uncover open sockets.
   *
   * @return  number of open connections.
   */
  // CheckStyle:MagicNumber OFF
  protected static int countOpenConnections()
  {
    try {
      final String[] cmd = new String[] {"netstat", "-al"};
      final Process p = new ProcessBuilder(cmd).start();
      final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line;
      final List<String> openConns = new ArrayList<>();
      while ((line = br.readLine()) != null) {
        if (line.matches("(.*)ESTABLISHED(.*)")) {
          final String s = line.split("\\s+")[4];
          openConns.add(s);
        }
      }

      int count = 0;
      for (String o : openConns) {
        if (o.contains("ldap")) {
          count++;
        }
      }
      return count;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
  // CheckStyle:MagicNumber ON


  /**
   * Container for a search result.
   */
  public static class Result
  {

    /** Reference to result. */
    private final Object result;


    /**
     * Create a new result.
     *
     * @param  o  result reference
     */
    public Result(final Object o)
    {
      result = o;
    }
  }


  /**
   * Container for a search entry.
   */
  public static class Entry
  {

    /**
     * Reference to entry.
     */
    private final Object entry;


    /**
     * Create a new entry.
     *
     * @param o entry reference
     */
    public Entry(final Object o)
    {
      entry = o;
    }
  }
}
