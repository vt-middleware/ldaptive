/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.RoundRobinConnectionStrategy;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.TestUtils;
import org.ldaptive.io.LdifWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for connection pools.
 *
 * @author  Middleware Services
 */
public class ConnectionPoolTest extends AbstractTest
{

  /** Entries for pool tests. */
  private static final Map<String, LdapEntry[]> ENTRIES = new HashMap<>();

  /**
   * Initialize the map of entries.
   */
  static {
    for (int i = 2; i <= 10; i++) {
      ENTRIES.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Base DN for searching. */
  private String searchBaseDn;

  /** LdapPool instance for concurrency testing. */
  private SoftLimitConnectionPool softLimitPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingConnectionPool blockingPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingConnectionPool blockingTimeoutPool;

  /** LdapPool instance for concurrency testing. */
  private BlockingConnectionPool connStrategyPool;

  /** Time in millis it takes the pool test to run. */
  private long softLimitRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingRuntime;

  /** Time in millis it takes the pool test to run. */
  private long blockingTimeoutRuntime;


  /**
   * @param  host  to connect to.
   * @param  dn  base DN for searching.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "ldapTestHost",
      "ldapBaseDn"
    }
  )
  @BeforeClass(
    groups = {
      "softlimitpool",
      "blockingpool",
      "blockingtimeoutpool",
      "connstrategypool"})
  public void createPools(final String host, final String dn)
    throws Exception
  {
    searchBaseDn = dn;

    final ConnectionConfig cc = TestUtils.readConnectionConfig(null);

    final PoolConfig softLimitPc = new PoolConfig();
    softLimitPc.setValidateOnCheckIn(true);
    softLimitPc.setValidateOnCheckOut(true);
    softLimitPc.setValidatePeriodically(true);
    softLimitPc.setValidatePeriod(5L);
    softLimitPool = new SoftLimitConnectionPool(softLimitPc, new DefaultConnectionFactory(cc));
    softLimitPool.setPruneStrategy(new IdlePruneStrategy(5L, 1L));
    softLimitPool.setValidator(new SearchValidator());

    final PoolConfig blockingPc = new PoolConfig();
    blockingPc.setValidateOnCheckIn(true);
    blockingPc.setValidateOnCheckOut(true);
    blockingPc.setValidatePeriodically(true);
    blockingPc.setValidatePeriod(5L);
    blockingPool = new BlockingConnectionPool(blockingPc, new DefaultConnectionFactory(cc));
    blockingPool.setPruneStrategy(new IdlePruneStrategy(5L, 1L));
    blockingPool.setValidator(new SearchValidator());

    final PoolConfig blockingTimeoutPc = new PoolConfig();
    blockingTimeoutPc.setValidateOnCheckIn(true);
    blockingTimeoutPc.setValidateOnCheckOut(true);
    blockingTimeoutPc.setValidatePeriodically(true);
    blockingTimeoutPc.setValidatePeriod(5L);
    blockingTimeoutPool = new BlockingConnectionPool(blockingTimeoutPc, new DefaultConnectionFactory(cc));
    blockingTimeoutPool.setPruneStrategy(new IdlePruneStrategy(5L, 1L));
    blockingTimeoutPool.setBlockWaitTime(1000L);
    blockingTimeoutPool.setValidator(new SearchValidator());

    final ConnectionConfig connStrategyCc = TestUtils.readConnectionConfig(null);
    connStrategyCc.setLdapUrl(String.format("%s ldap://dne.middleware.vt.edu", host));

    final DefaultConnectionFactory connStrategyCf = new DefaultConnectionFactory(connStrategyCc);
    connStrategyCf.getConnectionConfig().setConnectionStrategy(new RoundRobinConnectionStrategy());
    connStrategyPool = new BlockingConnectionPool(new PoolConfig(), connStrategyCf);
  }


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   * @param  ldifFile10  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createEntry2",
      "createEntry3",
      "createEntry4",
      "createEntry5",
      "createEntry6",
      "createEntry7",
      "createEntry8",
      "createEntry9",
      "createEntry10"
    }
  )
  @BeforeClass(
    groups = {
      "softlimitpool",
      "blockingpool",
      "blockingtimeoutpool",
      "connstrategypool"
      },
    dependsOnMethods = {"createPools"})
  // CheckStyle:ParameterNumber OFF
  public void createPoolEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    // CheckStyle:Indentation OFF
    ENTRIES.get("2")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile2)).getEntry();
    ENTRIES.get("3")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile3)).getEntry();
    ENTRIES.get("4")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile4)).getEntry();
    ENTRIES.get("5")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile5)).getEntry();
    ENTRIES.get("6")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile6)).getEntry();
    ENTRIES.get("7")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile7)).getEntry();
    ENTRIES.get("8")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile8)).getEntry();
    ENTRIES.get("9")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile9)).getEntry();
    ENTRIES.get("10")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile10)).getEntry();
    // CheckStyle:Indentation ON

    for (Map.Entry<String, LdapEntry[]> e : ENTRIES.entrySet()) {
      super.createLdapEntry(e.getValue()[0]);
    }

    softLimitPool.initialize();
    blockingPool.initialize();
    blockingTimeoutPool.initialize();
    connStrategyPool.initialize();
  }
  // CheckStyle:ParameterNumber ON


  /**
   * @param  ldifFile2  to load.
   * @param  ldifFile3  to load.
   * @param  ldifFile4  to load.
   * @param  ldifFile5  to load.
   * @param  ldifFile6  to load.
   * @param  ldifFile7  to load.
   * @param  ldifFile8  to load.
   * @param  ldifFile9  to load.
   * @param  ldifFile10  to load.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchResults2",
      "searchResults3",
      "searchResults4",
      "searchResults5",
      "searchResults6",
      "searchResults7",
      "searchResults8",
      "searchResults9",
      "searchResults10"
    }
  )
  @BeforeClass(
    groups = {
      "softlimitpool",
      "blockingpool",
      "blockingtimeoutpool",
      "connstrategypool"})
  // CheckStyle:ParameterNumber OFF
  public void loadPoolSearchResults(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    // CheckStyle:Indentation OFF
    ENTRIES.get("2")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile2)).getEntry();
    ENTRIES.get("3")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile3)).getEntry();
    ENTRIES.get("4")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile4)).getEntry();
    ENTRIES.get("5")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile5)).getEntry();
    ENTRIES.get("6")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile6)).getEntry();
    ENTRIES.get("7")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile7)).getEntry();
    ENTRIES.get("8")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile8)).getEntry();
    ENTRIES.get("9")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile9)).getEntry();
    ENTRIES.get("10")[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile10)).getEntry();
    // CheckStyle:Indentation ON
  }
  // CheckStyle:ParameterNumber ON


  /** @throws  Exception  On test failure. */
  @AfterClass(
    groups = {
      "softlimitpool",
      "blockingpool",
      "blockingtimeoutpool",
      "connstrategypool"})
  public void deletePoolEntry()
    throws Exception
  {
    super.deleteLdapEntry(ENTRIES.get("2")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("3")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("4")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("5")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("6")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("7")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("8")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("9")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("10")[0].getDn());

    softLimitPool.close();
    AssertJUnit.assertEquals(softLimitPool.availableCount(), 0);
    AssertJUnit.assertEquals(softLimitPool.activeCount(), 0);
    blockingPool.close();
    AssertJUnit.assertEquals(blockingPool.availableCount(), 0);
    AssertJUnit.assertEquals(blockingPool.activeCount(), 0);
    blockingTimeoutPool.close();
    AssertJUnit.assertEquals(blockingTimeoutPool.availableCount(), 0);
    AssertJUnit.assertEquals(blockingTimeoutPool.activeCount(), 0);
    connStrategyPool.close();
    AssertJUnit.assertEquals(connStrategyPool.availableCount(), 0);
    AssertJUnit.assertEquals(connStrategyPool.activeCount(), 0);
  }


  /**
   * Sample user data.
   *
   * @return  user data
   */
  @DataProvider(name = "pool-data")
  public Object[][] createPoolData()
  {
    return
      new Object[][] {
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=jadams@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("2")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=tjefferson@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("3")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=jmadison@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("4")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=jmonroe@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("5")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=jqadams@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("6")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=ajackson@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("7")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=mvburen@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn",
            "jpegPhoto"),
          ENTRIES.get("8")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=whharrison@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("9")[1],
        },
        {
          new SearchRequest(
            searchBaseDn,
            new SearchFilter("(mail=jtyler@ldaptive.org)"),
            "departmentNumber",
            "givenName",
            "sn"),
          ENTRIES.get("10")[1],
        },
      };
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"softlimitpool"})
  public void checkSoftLimitPoolImmutable()
    throws Exception
  {
    try {
      softLimitPool.getPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    try {
      softLimitPool.getConnectionFactory().getConnectionConfig().setConnectTimeout(10000);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"softlimitpool"}, dataProvider = "pool-data", threadPoolSize = 3, invocationCount = 50, timeOut = 60000
  )
  public void softLimitSmallSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    softLimitRuntime += search(softLimitPool, request, results);
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"softlimitpool"}, dataProvider = "pool-data", threadPoolSize = 10, invocationCount = 100, timeOut = 60000,
    dependsOnMethods = {"softLimitSmallSearch"}
  )
  public void softLimitMediumSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    softLimitRuntime += search(softLimitPool, request, results);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"softlimitpool"}, dependsOnMethods = {"softLimitMediumSearch"})
  public void softLimitMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, softLimitPool.activeCount());
    AssertJUnit.assertEquals(PoolConfig.DEFAULT_MIN_POOL_SIZE, softLimitPool.availableCount());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"blockingpool"})
  public void checkBlockingPoolImmutable()
    throws Exception
  {
    try {
      blockingPool.getPoolConfig().setMinPoolSize(8);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }

    try {
      blockingPool.getConnectionFactory().getConnectionConfig().setConnectTimeout(10000);
      AssertJUnit.fail("Expected illegalstateexception to be thrown");
    } catch (IllegalStateException e) {
      AssertJUnit.assertEquals(IllegalStateException.class, e.getClass());
    }
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"blockingpool"}, dataProvider = "pool-data", threadPoolSize = 3, invocationCount = 50, timeOut = 60000
  )
  public void blockingSmallSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    blockingRuntime += search(blockingPool, request, results);
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"blockingpool"}, dataProvider = "pool-data", threadPoolSize = 10, invocationCount = 100, timeOut = 60000,
    dependsOnMethods = {"blockingSmallSearch"}
  )
  public void blockingMediumSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    blockingRuntime += search(blockingPool, request, results);
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"blockingpool"}, dataProvider = "pool-data", threadPoolSize = 50, invocationCount = 1000, timeOut = 60000,
    dependsOnMethods = {"blockingMediumSearch"}
  )
  public void blockingLargeSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    blockingRuntime += search(blockingPool, request, results);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"blockingpool"}, dependsOnMethods = {"blockingLargeSearch"})
  public void blockingMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, blockingPool.activeCount());
    AssertJUnit.assertEquals(PoolConfig.DEFAULT_MIN_POOL_SIZE, blockingPool.availableCount());
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"blockingtimeoutpool"}, dataProvider = "pool-data", threadPoolSize = 3, invocationCount = 50,
    timeOut = 60000
  )
  public void blockingTimeoutSmallSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    try {
      blockingTimeoutRuntime += search(blockingTimeoutPool, request, results);
    } catch (BlockingTimeoutException e) {
      logger.warn("block timeout exceeded for small search", e);
    }
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"blockingtimeoutpool"}, dataProvider = "pool-data", threadPoolSize = 10, invocationCount = 100,
    timeOut = 60000, dependsOnMethods = {"blockingTimeoutSmallSearch"}
  )
  public void blockingTimeoutMediumSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    try {
      blockingTimeoutRuntime += search(blockingTimeoutPool, request, results);
    } catch (BlockingTimeoutException e) {
      logger.warn("block timeout exceeded for medium search", e);
    }
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"blockingtimeoutpool"}, dataProvider = "pool-data", threadPoolSize = 50, invocationCount = 1000,
    timeOut = 60000, dependsOnMethods = {"blockingTimeoutMediumSearch"}
  )
  public void blockingTimeoutLargeSearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    try {
      blockingTimeoutRuntime += search(blockingTimeoutPool, request, results);
    } catch (BlockingTimeoutException e) {
      logger.warn("block timeout exceeded for large search", e);
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"blockingtimeoutpool"}, dependsOnMethods = {"blockingTimeoutLargeSearch"})
  public void blockingTimeoutMaxClean()
    throws Exception
  {
    Thread.sleep(10000);
    AssertJUnit.assertEquals(0, blockingTimeoutPool.activeCount());
    AssertJUnit.assertEquals(PoolConfig.DEFAULT_MIN_POOL_SIZE, blockingTimeoutPool.availableCount());
  }


  /**
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"connstrategypool"}, dataProvider = "pool-data", threadPoolSize = 10, invocationCount = 100,
    timeOut = 60000
  )
  public void connStrategySearch(final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    search(connStrategyPool, request, results);
  }


  /**
   * @param  pool  to get ldap object from.
   * @param  request  to search with
   * @param  results  to expect from the search.
   *
   * @return  time it takes to checkout/search/checkin from the pool
   *
   * @throws  Exception  On test failure.
   */
  private long search(final ConnectionPool pool, final SearchRequest request, final LdapEntry results)
    throws Exception
  {
    final long startTime = System.currentTimeMillis();
    Connection conn = null;
    SearchResult result = null;
    try {
      logger.trace("waiting for pool checkout");
      conn = pool.getConnection();
      logger.trace("performing search: {}", request);

      final SearchOperation search = new SearchOperation(conn);
      result = search.execute(request).getResult();
      logger.trace("search completed: {}", result);
    } finally {
      logger.trace("returning connection to pool");
      if (conn != null) {
        conn.close();
      }
    }

    final StringWriter sw = new StringWriter();
    final LdifWriter lw = new LdifWriter(sw);
    lw.write(result);
    AssertJUnit.assertEquals(results, TestUtils.convertLdifToResult(sw.toString()).getEntry());
    return System.currentTimeMillis() - startTime;
  }
}
