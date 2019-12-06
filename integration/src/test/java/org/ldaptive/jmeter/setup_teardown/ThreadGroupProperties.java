/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.setup_teardown;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

/**
 * Class that provides access to ThreadGroup details for the purpose of reporting.
 *
 * @author Middleware Services
 */
public final class ThreadGroupProperties
{

  /** Number of Threads (Simulated Users) using SingleConnectionFactory for LDAP Searches */
  private static int searchNumSingleConnectionFactorynUsers;

  /** Number of Threads (Simulated Users) using PooledConnectionFactory for LDAP Searches */
  private static int searchNumPooledConnectionFactoryUsers;

  /** Number of Threads (Simulated Users) using DefaultConnectionFactory for LDAP Searches */
  private static int searchNumDefaultConnectionFactoryUsers;

  /** Number of Threads (Simulated Users) using DefaultConnectionFactory for LDAP Bind/Authentication */
  private static int authNumDefaultConnectionFactoryUsers;

  /** Number of seconds that each group will execute requests */
  private static int threadRunDuration;

  /**
   * How many seconds it will take each ThreadGroup to start ALL threads in their group.
   *
   * For example, if a thread group has 10 users and ramp-up is 5 seconds, the group will add a new thread every 0.5s
   */
  private static int threadRampUp;


  /**
   * Private constructor. All implementations must use {@link #ThreadGroupProperties(JavaSamplerContext)}
   * (JavaSamplerContext)}
   */
  private ThreadGroupProperties() {}


  /**
   * Instantiate and set ThreadGroup property details for reporting purposes.
   *
   * @param context current run context
   */
  public ThreadGroupProperties(final JavaSamplerContext context)
  {
    searchNumSingleConnectionFactorynUsers = intProp("NUM_SEARCH_SINGLE_USERS", context);
    searchNumPooledConnectionFactoryUsers = intProp("NUM_SEARCH_POOLED_USERS", context);
    searchNumDefaultConnectionFactoryUsers = intProp("NUM_SEARCH_DEFAULT_USERS", context);
    authNumDefaultConnectionFactoryUsers = intProp("NUM_AUTH_DEFAULT_USERS", context);
    threadRunDuration = intProp("THREAD_TOTAL_RUN_DURATION", context);
    threadRampUp = intProp("THREAD_USER_RAMP_UP", context);
  }


  private int intProp(final String propName, final JavaSamplerContext context)
  {
    return Integer.parseInt(context.getJMeterVariables().get(propName));
  }


  /**
   * @return String representation of details for all ThreadGroups
   */
  public String threadGroupPlanDetails()
  {
    final StringBuilder stringBuilder = new StringBuilder();
    final String singleSearch = threadGroupDetails(
      "Search: SingleConnectionFactory",
      searchNumSingleConnectionFactorynUsers,
      threadRunDuration, threadRampUp
    );
    final String pooledSearch = threadGroupDetails(
      "Search: PooledConnectionFactory",
      searchNumPooledConnectionFactoryUsers,
      threadRunDuration, threadRampUp
    );
    final String defaulSearch = threadGroupDetails(
      "Search: DefaultConnectionFactory",
      searchNumDefaultConnectionFactoryUsers,
      threadRunDuration, threadRampUp
    );
    final String defultAuth = threadGroupDetails(
      "Authentication: DefaultConnectionFactory",
      authNumDefaultConnectionFactoryUsers,
      threadRunDuration, threadRampUp
    );
    stringBuilder.append("Thread Group Details").append(System.lineSeparator())
      .append(singleSearch)
      .append(pooledSearch)
      .append(defaulSearch)
      .append(defultAuth);
    return stringBuilder.toString();
  }


  /**
   * @param name of ThreadGroup
   * @param numThreads that will be started for this group
   * @param runDuration number of seconds that this group will execute requests
   * @param rampUp number of seconds it will take to start all threads in this group
   * @return String representation of a single ThreadGroup details
   */
  private static String threadGroupDetails(
    final String name,
    final int numThreads,
    final int runDuration,
    final int rampUp)
  {
    return "\t" + name +
      System.lineSeparator() +
      "\t\t" + "Num Threads: " + numThreads +
      System.lineSeparator() +
      "\t\t" + "Thread Ramp-up: " + rampUp + " seconds" +
      System.lineSeparator() +
      "\t\t" + "Group Run Duration: " + runDuration + " seconds" +
      System.lineSeparator();
  }
}
