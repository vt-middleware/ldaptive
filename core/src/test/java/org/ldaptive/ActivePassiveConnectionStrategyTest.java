/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ActivePassiveConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategyTest
{


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
          "ldap://directory.ldaptive.org",
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
      };
  }


  /**
   * Unit test for {@link ActivePassiveConnectionStrategy#apply()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   */
  @Test(groups = "provider", dataProvider = "urls")
  public void apply(final String actual, final LdapURL[] expected)
  {
    final ActivePassiveConnectionStrategy strategy = new ActivePassiveConnectionStrategy();
    strategy.initialize(actual);
    Assert.assertEquals(strategy.apply(), Arrays.asList(expected));
    Assert.assertEquals(strategy.apply(), Arrays.asList(expected));
    Assert.assertEquals(strategy.apply(), Arrays.asList(expected));
    Assert.assertEquals(strategy.apply(), Arrays.asList(expected));
  }
}
