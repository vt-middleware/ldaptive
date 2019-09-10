/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
   * Unit test for {@link RandomConnectionStrategy#next(LdapURLSet)}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   *
   * @throws  LdapException  On LDAP operation errors.
   */
  @Test(groups = "conn", dataProvider = "urls")
  public void next(final String actual, final LdapURL[] expected) throws LdapException
  {
    final LdapURLSet urlSet = new LdapURLSet(new RandomConnectionStrategy(), actual);
    Assert.assertEquals(urlSet.getActiveUrls().size(), expected.length);
    int i = 0;
    final Map<LdapURL, Integer> indexMap = new HashMap<>();
    for (LdapURL url : expected) {
      Assert.assertEquals(urlSet.getActiveUrls().get(i), url);
      indexMap.put(url, i++);
    }
    // Ensure random distribution
    final int[] indices = new int[500];
    for (int j = 0; j < indices.length; j++) {
      final int finalJ = j;
      urlSet.doWithNextActiveUrl(u -> indices[finalJ] = indexMap.get(u));
    }
    Assert.assertTrue(isRandom(indices, expected.length));
  }

//
//  @Test(groups = "conn")
//  public void firstUrlInactive()
//    throws Exception
//  {
//    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
//    final ConnectionConfig cc = new ConnectionConfig();
//    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
//    cc.setConnectionStrategy(strategy);
//    final MockConnection conn = new MockConnection(cc);
//    conn.setOpenPredicate(new Predicate<>() {
//      private int count;
//
//      @Override
//      public boolean test(final LdapURL url)
//      {
//        if (count == 0) {
//          count++;
//          return false;
//        }
//        return true;
//      }
//    });
//    conn.setTestPredicate(ldapURL -> true);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//
//    // first entry should fail
//    conn.open();
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 2);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 1);
//
//    // confirm the inactive entry stays at the end
//    List<LdapURL> applyList = strategy.apply();
//    Assert.assertEquals(applyList.size(), 3);
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(0)));
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(1)));
//    Assert.assertTrue(
//      strategy.ldapURLSet.inactive.values().stream().map(
//        e -> e.getValue()).collect(Collectors.toList()).contains(applyList.get(2)));
//
//    // mark inactive entry as active
//    strategy.success(strategy.ldapURLSet.inactive.values().iterator().next().getValue());
//    applyList = strategy.apply();
//    Assert.assertEquals(applyList.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(0)));
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(1)));
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(2)));
//  }
//
//
//  @Test(groups = "conn")
//  public void firstAndSecondUrlInactive()
//    throws Exception
//  {
//    final RandomConnectionStrategy strategy = new RandomConnectionStrategy();
//    final ConnectionConfig cc = new ConnectionConfig();
//    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
//    cc.setConnectionStrategy(strategy);
//    final MockConnection conn = new MockConnection(cc);
//    conn.setOpenPredicate(new Predicate<>() {
//      private int count;
//
//      @Override
//      public boolean test(final LdapURL url)
//      {
//        if (count == 0 || count == 1) {
//          count++;
//          return false;
//        }
//        return true;
//      }
//    });
//    conn.setTestPredicate(ldapURL -> true);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//
//    // first and second entry should fail
//    conn.open();
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 1);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 2);
//
//    // confirm the inactive entries stay at the end
//    List<LdapURL> applyList = strategy.apply();
//    Assert.assertEquals(applyList.size(), 3);
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(0)));
//    Assert.assertTrue(
//      strategy.ldapURLSet.inactive.values().stream().map(
//        e -> e.getValue()).collect(Collectors.toList()).contains(applyList.get(1)));
//    Assert.assertTrue(
//      strategy.ldapURLSet.inactive.values().stream().map(
//        e -> e.getValue()).collect(Collectors.toList()).contains(applyList.get(2)));
//
//    // mark first entry as active
//    strategy.success(strategy.ldapURLSet.inactive.values().iterator().next().getValue());
//    applyList = strategy.apply();
//    Assert.assertEquals(applyList.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 2);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 1);
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(0)));
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(1)));
//    Assert.assertTrue(
//      strategy.ldapURLSet.inactive.values().stream().map(
//        e -> e.getValue()).collect(Collectors.toList()).contains(applyList.get(2)));
//
//    // mark second entry as active
//    strategy.success(strategy.ldapURLSet.inactive.values().iterator().next().getValue());
//    applyList = strategy.apply();
//    Assert.assertEquals(applyList.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(0)));
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(1)));
//    Assert.assertTrue(strategy.ldapURLSet.active.values().contains(applyList.get(2)));
//  }

  /**
   * Performs a simple test to determine whether the indices in the given array are randomly distributed
   * on the range [0, N), where N is the range of indices.
   *
   * @param  indices  Array of possibly randomly distributed indices.
   * @param  range  Range of index values in array.
   *
   * @return  True if all possible sequences of indices of length N appear in the array, false otherwise.
   */
  boolean isRandom(final int[] indices, final int range)
  {
    // Compute total number of permutations of sequences of indices
    int count = range;
    for (int i = 1; i < range; i++) {
      count *= range;
    }
    final int[][] permutations = new int[count][range];
    for (int i = 0; i < count; i++) {
      permutations[i] = digitsToArray(Integer.toString(i, range), range);
    }
    for (int[] permutation : permutations) {
      if (!contains(indices, permutation)) {
        return false;
      }
    }
    return true;
  }


  /**
   * Convert the digits of a number into an array of length <code>range</code> that is left-padded with zeroes
   *
   * @param  number  String of decimal digits.
   * @param  range  Range of digits that appear in the number.
   *
   * @return Array of decimal digits of length <code>range</code>.
   */
  private static int[] digitsToArray(final String number, final int range)
  {
    final int[] digits = new int[range];
    Arrays.fill(digits, 0);
    final int offset = range - number.length();
    for (int i = 0; i < number.length(); i++) {
      digits[offset + i] = Integer.parseInt(String.valueOf(number.charAt(i)));
    }
    return digits;
  }


  /**
   * Determines if the given sub-series appears in the series.
   *
   * @param  series  Series to check.
   * @param  subseries  The sub-series to search for.
   *
   * @return True if sub-series is found in the series, false otherwise.
   */
  private static boolean contains(final int[] series, final int[] subseries)
  {
    int j = 0;
    for (int i = 0; i < series.length - subseries.length; i++) {
      for (; j < subseries.length; j++) {
        if (series[i + j] != subseries[j]) {
          break;
        }
      }
      if (j == subseries.length) {
        return true;
      }
    }
    return false;
  }
}
