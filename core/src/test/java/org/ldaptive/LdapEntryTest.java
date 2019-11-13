/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.HashSet;
import java.util.Set;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapEntry}.
 *
 * @author  Middleware Services
 */
public class LdapEntryTest
{


  /**
   * Ldap attribute test data.
   *
   * @return  ldap attributes
   */
  @DataProvider(name = "attributes")
  public Object[][] createSerializable()
  {
    return new Object[][] {
      new Object[] {
        new LdapAttribute("givenName", "John"),
        new LdapAttribute("sn", "Doe"),
      },
    };
  }


  /** Tests default sort behavior. */
  @Test(groups = "bean")
  public void defaultSortBehavior()
  {
    final LdapEntry le = new LdapEntry("uid=1");
    AssertJUnit.assertEquals(SortBehavior.getDefaultSortBehavior(), le.getSortBehavior());
    AssertJUnit.assertEquals(0, le.size());
    AssertJUnit.assertNull(le.getAttribute());
    AssertJUnit.assertEquals("uid=1", le.getDn());
    le.setDn("uid=2");
    AssertJUnit.assertEquals("uid=2", le.getDn());
    le.clear();
    AssertJUnit.assertEquals(0, le.size());
  }


  /**
   * Tests ordered sort behavior.
   *
   * @param  attr1  ldap attribute
   * @param  attr2  ldap attribute
   */
  @Test(groups = "bean", dataProvider = "attributes")
  public void orderedSortBehavior(final LdapAttribute attr1, final LdapAttribute attr2)
  {
    final LdapEntry le = new LdapEntry(SortBehavior.ORDERED);
    AssertJUnit.assertEquals(SortBehavior.ORDERED, le.getSortBehavior());
    le.addAttribute(attr2, attr1);

    final LdapAttribute[] attrs = le.getAttributes().toArray(new LdapAttribute[2]);
    AssertJUnit.assertEquals(attr2, attrs[0]);
    AssertJUnit.assertEquals(attr1, attrs[1]);
    le.clear();
    AssertJUnit.assertEquals(0, le.size());
  }


  /**
   * Tests sorted sort behavior.
   *
   * @param  attr1  ldap attribute
   * @param  attr2  ldap attribute
   */
  @Test(groups = "bean", dataProvider = "attributes")
  public void sortedSortBehavior(final LdapAttribute attr1, final LdapAttribute attr2)
  {
    final LdapEntry le = new LdapEntry(SortBehavior.SORTED);
    AssertJUnit.assertEquals(SortBehavior.SORTED, le.getSortBehavior());
    le.addAttribute(attr2, attr1);

    final LdapAttribute[] attrs = le.getAttributes().toArray(new LdapAttribute[2]);
    AssertJUnit.assertEquals(attr1, attrs[0]);
    AssertJUnit.assertEquals(attr2, attrs[1]);
    le.clear();
    AssertJUnit.assertEquals(0, le.size());
  }


  /**
   * Tests create with one entry.
   *
   * @param  attr1  ldap attribute
   * @param  attr2  ldap attribute
   */
  @Test(groups = "bean", dataProvider = "attributes")
  public void createOne(final LdapAttribute attr1, final LdapAttribute attr2)
  {
    final LdapEntry le = new LdapEntry("uid=1", attr1);
    AssertJUnit.assertEquals(attr1, le.getAttribute());
    AssertJUnit.assertEquals(attr1, le.getAttribute("givenName"));
    AssertJUnit.assertEquals(attr1, le.getAttribute("givenname"));
    AssertJUnit.assertEquals("givenName", le.getAttributeNames()[0]);
    AssertJUnit.assertEquals(1, le.size());
    AssertJUnit.assertEquals(le, new LdapEntry("uid=1", attr1));
    le.clear();
    AssertJUnit.assertEquals(0, le.size());
  }


  /**
   * Tests create with two entries.
   *
   * @param  attr1  ldap attribute
   * @param  attr2  ldap attribute
   */
  @Test(groups = "bean", dataProvider = "attributes")
  public void createTwo(final LdapAttribute attr1, final LdapAttribute attr2)
  {
    final LdapEntry le = new LdapEntry("uid=1", attr2, attr1);
    AssertJUnit.assertEquals(attr1, le.getAttribute("givenName"));
    AssertJUnit.assertEquals(attr2, le.getAttribute("SN"));
    AssertJUnit.assertEquals(2, le.getAttributeNames().length);
    AssertJUnit.assertEquals(2, le.size());
    AssertJUnit.assertEquals(le, new LdapEntry("uid=1", attr1, attr2));
    le.removeAttribute(attr2);
    AssertJUnit.assertEquals(1, le.size());
    le.clear();
    AssertJUnit.assertEquals(0, le.size());
  }


  /**
   * Tests create with a collection.
   *
   * @param  attr1  ldap attribute
   * @param  attr2  ldap attribute
   */
  @Test(groups = "bean", dataProvider = "attributes")
  public void createCollection(final LdapAttribute attr1, final LdapAttribute attr2)
  {
    final Set<LdapAttribute> s = new HashSet<>();
    s.add(attr1);

    final LdapEntry le = new LdapEntry("uid=1", s);
    le.addAttribute(attr2);
    AssertJUnit.assertEquals(attr1, le.getAttribute("GIVENNAME"));
    AssertJUnit.assertEquals(attr2, le.getAttribute("sn"));
    AssertJUnit.assertEquals(2, le.getAttributeNames().length);
    AssertJUnit.assertEquals(2, le.size());
    AssertJUnit.assertEquals(le, new LdapEntry("uid=1", attr1, attr2));
    le.removeAttribute("GIVENNAME");
    AssertJUnit.assertEquals(1, le.size());
    le.clear();
    AssertJUnit.assertEquals(0, le.size());
  }


  /**
   * Tests attribute rename.
   *
   * @param  attr1  ldap attribute
   * @param  attr2  ldap attribute
   */
  @Test(groups = "bean", dataProvider = "attributes")
  public void renameAttribute(final LdapAttribute attr1, final LdapAttribute attr2)
  {
    final LdapEntry le = new LdapEntry("uid=1", attr2, attr1);
    AssertJUnit.assertEquals("givenName", le.getAttribute("givenname").getName());
    le.renameAttribute(null, "firstName");
    AssertJUnit.assertEquals("givenName", le.getAttribute("givenname").getName());
    le.renameAttribute("noName", "firstName");
    AssertJUnit.assertEquals("givenName", le.getAttribute("givenname").getName());

    final LdapAttribute la = le.getAttribute("givenName");
    le.renameAttribute("givenName", "firstName");
    AssertJUnit.assertNull(le.getAttribute("givenName"));
    AssertJUnit.assertNotNull(le.getAttribute("firstName"));
    AssertJUnit.assertEquals("firstName", la.getName());
    AssertJUnit.assertEquals("firstName", le.getAttribute("firstName").getName());
  }
}
