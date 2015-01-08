/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.HashSet;
import java.util.Set;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchResult}.
 *
 * @author  Middleware Services
 */
public class SearchResultTest
{


  /**
   * Ldap entry test data.
   *
   * @return  ldap entries
   */
  @DataProvider(name = "entries")
  public Object[][] createSerializable()
  {
    return
      new Object[][] {
        new Object[] {new LdapEntry("uid=1"), new LdapEntry("uid=2")},
      };
  }


  /**
   * Tests default sort behavior.
   */
  @Test(groups = {"bean"})
  public void defaultSortBehavior()
  {
    final SearchResult sr = new SearchResult();
    AssertJUnit.assertEquals(
      SortBehavior.getDefaultSortBehavior(),
      sr.getSortBehavior());
    AssertJUnit.assertEquals(0, sr.size());
    AssertJUnit.assertNull(sr.getEntry());
    sr.clear();
    AssertJUnit.assertEquals(0, sr.size());
  }


  /**
   * Tests ordered sort behavior.
   *
   * @param  entry1  ldap entry
   * @param  entry2  ldap entry
   */
  @Test(groups = {"bean"}, dataProvider = "entries")
  public void orderedSortBehavior(
    final LdapEntry entry1,
    final LdapEntry entry2)
  {
    final SearchResult sr = new SearchResult(SortBehavior.ORDERED);
    AssertJUnit.assertEquals(SortBehavior.ORDERED, sr.getSortBehavior());
    sr.addEntry(entry2, entry1);

    final LdapEntry[] entries = sr.getEntries().toArray(new LdapEntry[2]);
    AssertJUnit.assertEquals(entry2, entries[0]);
    AssertJUnit.assertEquals(entry1, entries[1]);
    sr.clear();
    AssertJUnit.assertEquals(0, sr.size());
  }


  /**
   * Tests sorted sort behavior.
   *
   * @param  entry1  ldap entry
   * @param  entry2  ldap entry
   */
  @Test(groups = {"bean"}, dataProvider = "entries")
  public void sortedSortBehavior(
    final LdapEntry entry1,
    final LdapEntry entry2)
  {
    final SearchResult sr = new SearchResult(SortBehavior.SORTED);
    AssertJUnit.assertEquals(SortBehavior.SORTED, sr.getSortBehavior());
    sr.addEntry(entry2, entry1);
    final LdapEntry[] entries = sr.getEntries().toArray(new LdapEntry[2]);
    AssertJUnit.assertEquals(entry1, entries[0]);
    AssertJUnit.assertEquals(entry2, entries[1]);
    sr.clear();
    AssertJUnit.assertEquals(0, sr.size());
  }


  /**
   * Tests create with one entry.
   *
   * @param  entry1  ldap entry
   * @param  entry2  ldap entry
   */
  @Test(groups = {"bean"}, dataProvider = "entries")
  public void createOne(
    final LdapEntry entry1,
    final LdapEntry entry2)
  {
    final SearchResult sr = new SearchResult(entry1);
    AssertJUnit.assertEquals(entry1, sr.getEntry());
    AssertJUnit.assertEquals(entry1, sr.getEntry("uid=1"));
    AssertJUnit.assertEquals(entry1, sr.getEntry("UID=1"));
    AssertJUnit.assertEquals("uid=1", sr.getEntryDns()[0]);
    AssertJUnit.assertEquals(1, sr.size());
    AssertJUnit.assertEquals(sr, new SearchResult(entry1));
    sr.clear();
    AssertJUnit.assertEquals(0, sr.size());
  }


  /**
   * Tests create with two entries.
   *
   * @param  entry1  ldap entry
   * @param  entry2  ldap entry
   */
  @Test(groups = {"bean"}, dataProvider = "entries")
  public void createTwo(
    final LdapEntry entry1,
    final LdapEntry entry2)
  {
    final SearchResult sr = new SearchResult(entry2, entry1);
    AssertJUnit.assertEquals(entry1, sr.getEntry("uid=1"));
    AssertJUnit.assertEquals(entry2, sr.getEntry("UID=2"));
    AssertJUnit.assertEquals(2, sr.getEntryDns().length);
    AssertJUnit.assertEquals(2, sr.size());
    AssertJUnit.assertEquals(sr, new SearchResult(entry1, entry2));
    sr.removeEntry(entry2);
    AssertJUnit.assertEquals(1, sr.size());
    sr.clear();
    AssertJUnit.assertEquals(0, sr.size());
  }


  /**
   * Tests create with a collection.
   *
   * @param  entry1  ldap entry
   * @param  entry2  ldap entry
   */
  @Test(groups = {"bean"}, dataProvider = "entries")
  public void createCollection(
    final LdapEntry entry1,
    final LdapEntry entry2)
  {
    final Set<LdapEntry> s = new HashSet<>();
    s.add(entry1);
    final SearchResult sr = new SearchResult(s);
    sr.addEntry(entry2);
    AssertJUnit.assertEquals(entry1, sr.getEntry("UID=1"));
    AssertJUnit.assertEquals(entry2, sr.getEntry("uid=2"));
    AssertJUnit.assertEquals(2, sr.getEntryDns().length);
    AssertJUnit.assertEquals(2, sr.size());
    AssertJUnit.assertEquals(sr, new SearchResult(entry1, entry2));
    sr.removeEntry("UID=1");
    AssertJUnit.assertEquals(1, sr.size());
    sr.clear();
    AssertJUnit.assertEquals(0, sr.size());
  }


  /**
   * Unit test for {@link SearchResult#subResult(int, int)}.
   *
   * @param  entry1  ldap entry
   * @param  entry2  ldap entry
   */
  @Test(groups = {"bean"}, dataProvider = "entries")
  public void subResult(
    final LdapEntry entry1,
    final LdapEntry entry2)
  {
    final SearchResult sr = new SearchResult(SortBehavior.SORTED);
    sr.addEntry(entry2, entry1);
    AssertJUnit.assertEquals(0, sr.subResult(2, 2).size());
    AssertJUnit.assertEquals(1, sr.subResult(1, 2).size());
    AssertJUnit.assertEquals(2, sr.subResult(0, 2).size());
    try {
      sr.subResult(-1, 1);
      AssertJUnit.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IndexOutOfBoundsException.class, e.getClass());
    }
    try {
      sr.subResult(0, 3);
      AssertJUnit.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IndexOutOfBoundsException.class, e.getClass());
    }
    try {
      sr.subResult(1, 0);
      AssertJUnit.fail("Should have thrown IndexOutOfBoundsException");
    } catch (Exception e) {
      AssertJUnit.assertEquals(IndexOutOfBoundsException.class, e.getClass());
    }
  }
}
