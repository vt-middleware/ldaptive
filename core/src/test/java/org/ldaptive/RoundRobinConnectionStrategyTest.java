/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.ldaptive.provider.mock.MockConnection;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link RoundRobinConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategyTest
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
          0,
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
          0,
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
          0,
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
            new LdapURL("ldap://directory-1.ldaptive.org"),
          },
          1,
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-3.ldaptive.org"),
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
          2,
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
          3,
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
            new LdapURL("ldap://directory-1.ldaptive.org"),
          },
          4,
        },
      };
  }


  /**
   * Unit test for {@link RoundRobinConnectionStrategy#iterator()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   * @param  count  number of times to invoke {@link ConnectionStrategy#iterator()}
   */
  @Test(groups = "conn", dataProvider = "urls")
  public void iterator(final String actual, final LdapURL[] expected, final int count)
  {
    final RoundRobinConnectionStrategy strategy = new RoundRobinConnectionStrategy();
    strategy.initialize(actual, ldapURL -> true);
    for (int i = 0; i < count; i++) {
      strategy.iterator();
    }
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()), Arrays.asList(expected));
  }


  /**
   * Unit test for {@link RoundRobinConnectionStrategy#iterator()} ()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   * @param  count  number of times to invoke {@link ConnectionStrategy#iterator()}
   */
  @Test(groups = "conn", dataProvider = "urls")
  public void hasNext(final String actual, final LdapURL[] expected, final int count)
  {
    final RoundRobinConnectionStrategy strategy = new RoundRobinConnectionStrategy();
    strategy.initialize(actual, ldapURL -> true);
    final Iterator<LdapURL> iter = strategy.iterator();
    for (int i = 0; i < count; i++) {
      strategy.iterator();
    }
    int i = 0;
    while (iter.hasNext()) {
      iter.next();
      i++;
    }
    Assert.assertEquals(i, expected.length);
  }


  @Test(groups = "conn")
  public void firstUrlInactive()
    throws Exception
  {
    final RoundRobinConnectionStrategy strategy = new RoundRobinConnectionStrategy();
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(ldapURL -> !ldapURL.getHostname().contains("-1"));
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first entry should fail, list should reorder with that entry last
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-1.ldaptive.org"));

    // confirm the inactive entry stays at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-3.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-1.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-1.ldaptive.org"));

    // mark first entry as active, list should reorder with that entry last
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-3.ldaptive.org"),
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
  }


  @Test(groups = "conn")
  public void firstAndSecondUrlInactive()
    throws Exception
  {
    final RoundRobinConnectionStrategy strategy = new RoundRobinConnectionStrategy();
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(ldapURL -> !ldapURL.getHostname().contains("-1") && !ldapURL.getHostname().contains("-2"));
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first and second entry should fail, list should reorder with those entries last
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls().iterator().next(),
      new LdapURL("ldap://directory-3.ldaptive.org"));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org")));

    // confirm the inactive entries stay at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-3.ldaptive.org"),
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls().iterator().next(),
      new LdapURL("ldap://directory-3.ldaptive.org"));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org")));

    // mark first entry as active, list should reorder with that entry as the last active
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-2.ldaptive.org"));

    // confirm the inactive entries stay at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-3.ldaptive.org"),
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-2.ldaptive.org"));

    // mark second entry as active, list should reorder with that entry as the last active
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org"),
        new LdapURL("ldap://directory-1.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org"),
        new LdapURL("ldap://directory-2.ldaptive.org"),
        new LdapURL("ldap://directory-3.ldaptive.org")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
  }
}
