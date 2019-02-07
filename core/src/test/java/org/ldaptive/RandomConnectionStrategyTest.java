/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link RandomConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class RandomConnectionStrategyTest
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
   * Unit test for {@link RandomConnectionStrategy#apply()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   */
  @Test(groups = "provider", dataProvider = "urls")
  public void apply(final String actual, final LdapURL[] expected)
  {
    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
    strategy.initialize(actual);
    Assert.assertEquals(
      new HashSet<>(strategy.apply()), Stream.of(expected).collect(Collectors.toSet()));
    Assert.assertEquals(
      new HashSet<>(strategy.apply()), Stream.of(expected).collect(Collectors.toSet()));
    Assert.assertEquals(
      new HashSet<>(strategy.apply()), Stream.of(expected).collect(Collectors.toSet()));
    Assert.assertEquals(
      new HashSet<>(strategy.apply()), Stream.of(expected).collect(Collectors.toSet()));
  }
}
