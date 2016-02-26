/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ModifyOperation}.
 *
 * @author  Middleware Services
 */
public class ModifyOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry4")
  @BeforeClass(groups = {"modify"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"modify"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributeDn", "addAttributeAttribute" })
  @Test(groups = {"modify"})
  public void addAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = TestUtils.convertStringToEntry(dn, attrs);
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(dn, new AttributeModification(AttributeModificationType.ADD, expected.getAttribute())));

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(dn, new String[] {expected.getAttribute().getName()})).getResult();
      AssertJUnit.assertEquals(expected.getAttribute(), result.getEntry().getAttribute());
    }
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributesDn", "addAttributesAttributes" })
  @Test(groups = {"modify"})
  public void addAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = TestUtils.convertStringToEntry(dn, attrs);
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      final AttributeModification[] mods = new AttributeModification[expected.size()];
      int i = 0;
      for (LdapAttribute la : expected.getAttributes()) {
        mods[i] = new AttributeModification(AttributeModificationType.ADD, la);
        i++;
      }
      modify.execute(new ModifyRequest(dn, mods));

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(dn, expected.getAttributeNames())).getResult();
      TestUtils.assertEquals(expected, result.getEntry());
    }
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributeDn", "replaceAttributeAttribute" })
  @Test(groups = {"modify"}, dependsOnMethods = {"addAttribute"})
  public void replaceAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = TestUtils.convertStringToEntry(dn, attrs);
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(dn, new AttributeModification(AttributeModificationType.REPLACE, expected.getAttribute())));

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(dn, new String[] {expected.getAttribute().getName()})).getResult();
      TestUtils.assertEquals(expected, result.getEntry());
    }
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributesDn", "replaceAttributesAttributes" })
  @Test(groups = {"modify"}, dependsOnMethods = {"addAttributes"})
  public void replaceAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = TestUtils.convertStringToEntry(dn, attrs);
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      final AttributeModification[] mods = new AttributeModification[expected.size()];
      int i = 0;
      for (LdapAttribute la : expected.getAttributes()) {
        mods[i] = new AttributeModification(AttributeModificationType.REPLACE, la);
        i++;
      }
      modify.execute(new ModifyRequest(dn, mods));

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(dn, expected.getAttributeNames())).getResult();
      TestUtils.assertEquals(expected, result.getEntry());
    }
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributeDn", "removeAttributeAttribute" })
  @Test(groups = {"modify"}, dependsOnMethods = {"replaceAttribute"})
  public void removeAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = TestUtils.convertStringToEntry(dn, attrs);

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(dn, new AttributeModification(AttributeModificationType.REMOVE, expected.getAttribute())));

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(dn, new String[] {expected.getAttribute().getName()})).getResult();
      AssertJUnit.assertEquals(0, result.getEntry().getAttributes().size());
    }
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributesDn", "removeAttributesAttributes" })
  @Test(groups = {"modify"}, dependsOnMethods = {"replaceAttributes"})
  public void removeAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = TestUtils.convertStringToEntry(dn, attrs);
    final LdapEntry remove = TestUtils.convertStringToEntry(dn, attrs);

    final String[] attrsName = remove.getAttributeNames();
    remove.getAttributes().remove(remove.getAttribute(attrsName[0]));
    expected.getAttributes().remove(expected.getAttribute(attrsName[1]));

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      final AttributeModification[] mods = new AttributeModification[expected.size()];
      int i = 0;
      for (LdapAttribute la : remove.getAttributes()) {
        mods[i++] = new AttributeModification(AttributeModificationType.REMOVE, la);
      }
      modify.execute(new ModifyRequest(dn, mods));

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(dn, expected.getAttributeNames())).getResult();
      TestUtils.assertEquals(expected, result.getEntry());
    }
  }
}
