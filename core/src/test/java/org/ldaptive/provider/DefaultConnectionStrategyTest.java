/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DefaultConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class DefaultConnectionStrategyTest
{

  /** Strategy to test. */
  private final DefaultConnectionStrategy strategy =
    new DefaultConnectionStrategy();


  /**
   * URL test data.
   *
   * @return  test data
   */
  @DataProvider(name = "urls")
  public Object[][] createURLs()
  {
    return
      new Object[][] {
        new Object[] {
          new TestConnectionFactoryMetadata(),
          null,
        },
        new Object[] {
          new TestConnectionFactoryMetadata("ldap://directory.ldaptive.org"),
          new String[] {"ldap://directory.ldaptive.org"},
        },
        new Object[] {
          new TestConnectionFactoryMetadata(
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new String[] {
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org",
          },
        },
        new Object[] {
          new TestConnectionFactoryMetadata(
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org " +
            "ldap://directory-3.ldaptive.org",
            3),
          new String[] {
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org " +
              "ldap://directory-3.ldaptive.org",
          },
        },
      };
  }


  /**
   * @param  metadata  to get ldap urls from
   * @param  urls  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"provider"},
    dataProvider = "urls"
  )
  public void getLdapUrls(
    final ConnectionFactoryMetadata metadata,
    final String[] urls)
    throws Exception
  {
    Assert.assertEquals(strategy.getLdapUrls(metadata), urls);
  }
}
