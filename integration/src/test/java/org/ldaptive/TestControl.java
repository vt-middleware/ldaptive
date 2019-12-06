/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

/**
 * Contains functions that run before and after all tests.
 *
 * @author  Middleware Services
 */
public class TestControl
{

  /** Type of directory being tested. */
  private static String directoryType;


  /**
   * Used by tests to determine if Active Directory is being tested.
   *
   * @return  whether active directory is being tested
   */
  public static boolean isActiveDirectory()
  {
    return "ACTIVE_DIRECTORY".equals(directoryType);
  }


  /**
   * Used by tests to determine if Oracle Directory server is being tested.
   *
   * @return  whether oracle directory server is being tested
   */
  public static boolean isOracleDirectory()
  {
    return "ORACLE".equals(directoryType);
  }


  /**
   * Obtains the lock before running all tests.
   *
   * @param  bindDn  to lock on
   *
   * @throws  Exception  on test failure
   */
  @BeforeSuite(alwaysRun = true)
  @Parameters("ldapBindDn")
  public void setup(final String bindDn)
    throws Exception
  {
    directoryType = "LDAP";
    /*
    if (isAD(TestUtils.createSetupConnectionFactory(), bindDn)) {
      directoryType = "ACTIVE_DIRECTORY";
    } else if (isOracle(TestUtils.createSetupConnectionFactory())) {
      directoryType = "ORACLE";
    } else {
      directoryType = "LDAP";
    }
    */
  }


  /**
   * Performs an object level search for the sAMAccountName attribute used by Active Directory.
   *
   * @param  cf  to perform compare with
   * @param  bindDn  to perform search on
   *
   * @return  whether the supplied entry is in active directory
   *
   * @throws  Exception  On failure.
   */
  protected boolean isAD(final ConnectionFactory cf, final String bindDn)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(cf);
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(
      bindDn,
      ReturnAttributes.NONE.value(),
      "(sAMAccountName=*)");
    return search.execute(request).entrySize() == 1;
  }


  /**
   * Performs an object level search on the root DSE for the vendorName attribute used by Oracle DS.
   *
   * @param  cf  to perform compare with
   *
   * @return  whether the supplied entry contains a vendorName attribute identified by Oracle
   *
   * @throws  Exception  On failure.
   */
  protected boolean isOracle(final ConnectionFactory cf)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(cf);
    final SearchRequest request = SearchRequest.objectScopeSearchRequest("", new String[] {"vendorName"});
    final LdapEntry rootDSE = search.execute(request).getEntry();
    return
      rootDSE.getAttribute("vendorName") != null &&
        rootDSE.getAttribute("vendorName").getStringValue().contains("Oracle");
  }
}
