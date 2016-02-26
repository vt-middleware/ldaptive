/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ldapi;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.provider.jndi.JndiProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Performance test for an ldap search operation over native sockets.
 *
 * @author  Middleware Services
 */
public class PerformanceTest
{

  /** LDAP host. */
  private String ldapHost;

  /** LDAP base dn. */
  private String ldapBaseDn;

  /** LDAP search filter. */
  private String ldapSearchFilter;


  /**
   * Initialize this test.
   *
   * @param  socketFile  file system location of the unix domain socket
   * @param  host  for comparison
   * @param  baseDn  to search on
   * @param  searchFilter  to execute
   */
  @BeforeClass(groups = {"ldapi"})
  @Parameters(
    {
      "ldapSocketFile",
      "ldapTestHost",
      "ldapBaseDn",
      "ldapSearchFilter"
    }
    )
  public void initialize(final String socketFile, final String host, final String baseDn, final String searchFilter)
  {
    System.setProperty("org.ldaptive.ldapi.socketFile", socketFile);
    ldapHost = host;
    ldapBaseDn = baseDn;
    ldapSearchFilter = searchFilter;
  }


  /**
   * Test connection factories.
   *
   * @return  test data
   *
   * @throws  Exception  on test failure
   */
  @DataProvider(name = "factories")
  @SuppressWarnings("unchecked")
  public Object[][] connectionFactories()
    throws Exception
  {
    final Map<String, Object> props = new HashMap<>();
    props.put(JndiProvider.SOCKET_FACTORY, "org.ldaptive.ldapi.AFUnixSocketFactory");

    final DefaultConnectionFactory nativeFactory = new DefaultConnectionFactory(
      new ConnectionConfig("ldap://domainsocket"));
    nativeFactory.getProvider().getProviderConfig().setProperties(props);

    final DefaultConnectionFactory localFactory = new DefaultConnectionFactory(new ConnectionConfig(ldapHost));

    final SearchRequest request = new SearchRequest(ldapBaseDn, ldapSearchFilter);
    return new Object[][] {
      new Object[] {nativeFactory, request, },
      new Object[] {localFactory, request, },
    };
  }


  /**
   * Searches with the supplied connection factory.
   *
   * @param  connFactory  to get connections from
   * @param  request  to execute
   *
   * @throws  Exception  On test errors.
   */
  @Test(groups = {"ldapi"}, dataProvider = "factories")
  public void search(final ConnectionFactory connFactory, final SearchRequest request)
    throws Exception
  {
    final long beforeTS = System.currentTimeMillis();
    try (Connection conn = connFactory.getConnection()) {
      conn.open();
      for (int i = 0; i < 10000; i++) {
        final SearchOperation search = new SearchOperation(conn);
        final SearchResult result = search.execute(request).getResult();
        result.getEntries().forEach(org.ldaptive.LdapEntry::toString);
      }
    }

    final long afterTS = System.currentTimeMillis();
    System.out.println(String.format("Total Execution Time %s ms for %s", afterTS - beforeTS, connFactory));
  }
}
