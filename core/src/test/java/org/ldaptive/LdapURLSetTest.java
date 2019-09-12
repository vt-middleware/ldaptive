/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapURLSet}.
 *
 * @author  Middleware Services
 */
public class LdapURLSetTest
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
        // active passive
        new Object[] {
          new LdapURLSet(
            new ActivePassiveConnectionStrategy(),
            "ldap://directory.ldaptive.org"),
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          new LdapURLSet(
            new ActivePassiveConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          new LdapURLSet(
            new ActivePassiveConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
        // random
        new Object[] {
          new LdapURLSet(
            new RandomConnectionStrategy(),
            "ldap://directory.ldaptive.org"),
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          new LdapURLSet(
            new RandomConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          new LdapURLSet(
            new RandomConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
        // round robin
        new Object[] {
          new LdapURLSet(
            new RoundRobinConnectionStrategy(),
            "ldap://directory.ldaptive.org"),
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          new LdapURLSet(
            new RoundRobinConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          new LdapURLSet(
            new RoundRobinConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
      };
  }


  @Test(groups = "conn", dataProvider = "urls")
  public void populate(final LdapURLSet urlSet, final LdapURL[] expected)
    throws Exception
  {
    Assert.assertTrue(urlSet.hasActiveUrls());
    Assert.assertEquals(urlSet.getActiveUrls().size(), expected.length);
    Assert.assertFalse(urlSet.hasInactiveUrls());
    Assert.assertEquals(urlSet.getInactiveUrls().size(), 0);
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(urlSet.getActiveUrls().get(i), expected[i]);
    }
  }


  @Test(groups = "conn", dataProvider = "urls")
  public void deactivate(final LdapURLSet urlSet, final LdapURL[] expected)
    throws Exception
  {
    Assert.assertTrue(urlSet.hasActiveUrls());
    Assert.assertEquals(urlSet.getActiveUrls().size(), expected.length);
    Assert.assertFalse(urlSet.hasInactiveUrls());
    Assert.assertEquals(urlSet.getInactiveUrls().size(), 0);

    // deactivate URLs
    while (urlSet.hasActiveUrls()) {
      urlSet.getActiveUrls().get(0).deactivate();
    }
    Assert.assertFalse(urlSet.hasActiveUrls());
    Assert.assertEquals(urlSet.getActiveUrls().size(), 0);
    Assert.assertTrue(urlSet.hasInactiveUrls());
    Assert.assertEquals(urlSet.getInactiveUrls().size(), expected.length);

    // activate URLs
    while (urlSet.hasInactiveUrls()) {
      urlSet.getInactiveUrls().get(0).activate();
    }
    Assert.assertTrue(urlSet.hasActiveUrls());
    Assert.assertEquals(urlSet.getActiveUrls().size(), expected.length);
    Assert.assertFalse(urlSet.hasInactiveUrls());
    Assert.assertEquals(urlSet.getInactiveUrls().size(), 0);
  }
}
