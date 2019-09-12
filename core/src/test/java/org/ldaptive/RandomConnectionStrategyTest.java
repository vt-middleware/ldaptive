/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.ldaptive.provider.mock.MockConnection;
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
   * Unit test for {@link RandomConnectionStrategy#iterator()} ()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   */
  @Test(groups = "conn", dataProvider = "urls")
  public void iterator(final String actual, final LdapURL[] expected)
  {
    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
    strategy.initialize(actual, ldapURL -> true);
    Assert.assertEquals(
      (Collection) StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toCollection(HashSet::new)),
      (Collection) Stream.of(expected).collect(Collectors.toCollection(HashSet::new)));
    Assert.assertEquals(
      (Collection) StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toCollection(HashSet::new)),
      (Collection) Stream.of(expected).collect(Collectors.toCollection(HashSet::new)));
    Assert.assertEquals(
      (Collection) StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toCollection(HashSet::new)),
      (Collection) Stream.of(expected).collect(Collectors.toCollection(HashSet::new)));
    Assert.assertEquals(
      (Collection) StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toCollection(HashSet::new)),
      (Collection) Stream.of(expected).collect(Collectors.toCollection(HashSet::new)));
    Assert.assertEquals(
      (Collection) StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toCollection(HashSet::new)),
      (Collection) Stream.of(expected).collect(Collectors.toCollection(HashSet::new)));
  }


  /**
   * Unit test for {@link RandomConnectionStrategy#iterator()} ()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   */
  @Test(groups = "conn", dataProvider = "urls")
  public void hasNext(final String actual, final LdapURL[] expected)
  {
    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
    strategy.initialize(actual, ldapURL -> true);
    final Iterator<LdapURL> iter = strategy.iterator();
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
    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(new Predicate<>() {
      private int count;

      @Override
      public boolean test(final LdapURL url)
      {
        if (count == 0) {
          count++;
          return false;
        }
        return true;
      }
    });
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first entry should fail
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);

    // confirm the inactive entry stays at the end
    List<LdapURL> applyList = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(applyList.size(), 3);
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(0)));
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(1)));
    Assert.assertTrue(
      strategy.ldapURLSet.getInactiveUrls().contains(applyList.get(2)));

    // mark inactive entry as active
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    applyList = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(applyList.size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(0)));
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(1)));
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(2)));
  }


  @Test(groups = "conn")
  public void firstAndSecondUrlInactive()
    throws Exception
  {
    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(new Predicate<>() {
      private int count;

      @Override
      public boolean test(final LdapURL url)
      {
        if (count == 0 || count == 1) {
          count++;
          return false;
        }
        return true;
      }
    });
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first and second entry should fail
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 1);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);

    // confirm the inactive entries stay at the end
    List<LdapURL> applyList = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(applyList.size(), 3);
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(0)));
    Assert.assertTrue(
      strategy.ldapURLSet.getInactiveUrls().contains(applyList.get(1)));
    Assert.assertTrue(
      strategy.ldapURLSet.getInactiveUrls().contains(applyList.get(2)));

    // mark first entry as active
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    applyList = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(applyList.size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(0)));
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(1)));
    Assert.assertTrue(
      strategy.ldapURLSet.getInactiveUrls().contains(applyList.get(2)));

    // mark second entry as active
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    applyList = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(applyList.size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(0)));
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(1)));
    Assert.assertTrue(strategy.ldapURLSet.getActiveUrls().contains(applyList.get(2)));
  }
}
