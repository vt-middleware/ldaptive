/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.dn.Dn;
import org.ldaptive.referral.FollowModifyReferralHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

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
  @BeforeClass(groups = "modify")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = readFileIntoString(ldifFile);
    testLdapEntry = convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "modify")
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
  @Test(groups = "modify")
  public void addAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);
    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    modify.execute(
      new ModifyRequest(dn, new AttributeModification(AttributeModification.Type.ADD, expected.getAttribute())));

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, new String[] {expected.getAttribute().getName()}));
    assertThat(result.getEntry().getAttribute()).isEqualTo(expected.getAttribute());
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributesDn", "addAttributesAttributes" })
  @Test(groups = "modify")
  public void addAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);
    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    final AttributeModification[] mods = new AttributeModification[expected.size()];
    int i = 0;
    for (LdapAttribute la : expected.getAttributes()) {
      mods[i] = new AttributeModification(AttributeModification.Type.ADD, la);
      i++;
    }
    modify.execute(new ModifyRequest(dn, mods));

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, expected.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(expected);
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to add.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "addAttributesDn", "addAttributesAttributes" })
  @Test(groups = "modify", dependsOnMethods = "addAttribute")
  public void addAttributesReferral(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);
    final LdapEntry referralExpected = convertStringToEntry(dn, attrs.replace("5", "6"));
    referralExpected.getAttributes()
      .forEach(a -> expected.getAttribute(a.getName()).addStringValues(a.getStringValues()));
    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    final AttributeModification[] mods = new AttributeModification[referralExpected.size()];
    int i = 0;
    for (LdapAttribute la : referralExpected.getAttributes()) {
      mods[i] = new AttributeModification(AttributeModification.Type.ADD, la);
      i++;
    }

    final Dn origDn = new Dn(dn);
    final String referralDn =
      Dn.builder().add(origDn.subDn(0, 1)).add("ou=referrals").add(origDn.subDn(2)).build().format();
    ModifyResponse res = modify.execute(new ModifyRequest(referralDn, mods));
    assertThat(res.getResultCode()).isEqualTo(ResultCode.REFERRAL);
    modify.setReferralResultHandler(new FollowModifyReferralHandler(url -> {
      final ConnectionConfig refConfig = ConnectionConfig.copy(cf.getConnectionConfig());
      refConfig.setLdapUrl(url.replace("localhost", new LdapURL(cf.getConnectionConfig().getLdapUrl()).getHostname()));
      return new DefaultConnectionFactory(refConfig);
    }));
    res = modify.execute(new ModifyRequest(referralDn, mods));
    assertThat(res.getResultCode()).isEqualTo(ResultCode.SUCCESS);

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, referralExpected.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(expected);
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributeDn", "replaceAttributeAttribute" })
  @Test(groups = "modify", dependsOnMethods = "addAttribute")
  public void replaceAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);
    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    modify.execute(
      new ModifyRequest(dn, new AttributeModification(AttributeModification.Type.REPLACE, expected.getAttribute())));

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, new String[] {expected.getAttribute().getName()}));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(expected);
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to replace.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "replaceAttributesDn", "replaceAttributesAttributes" })
  @Test(groups = "modify", dependsOnMethods = "addAttributes")
  public void replaceAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);
    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    final AttributeModification[] mods = new AttributeModification[expected.size()];
    int i = 0;
    for (LdapAttribute la : expected.getAttributes()) {
      mods[i] = new AttributeModification(AttributeModification.Type.REPLACE, la);
      i++;
    }
    modify.execute(new ModifyRequest(dn, mods));

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, expected.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(expected);
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributeDn", "removeAttributeAttribute" })
  @Test(groups = "modify", dependsOnMethods = "replaceAttribute")
  public void removeAttribute(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);

    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    modify.execute(
      new ModifyRequest(dn, new AttributeModification(AttributeModification.Type.DELETE, expected.getAttribute())));

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, new String[] {expected.getAttribute().getName()}));
    assertThat(result.getEntry().getAttributes().size()).isEqualTo(0);
  }


  /**
   * @param  dn  to modify.
   * @param  attrs  to remove.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "removeAttributesDn", "removeAttributesAttributes" })
  @Test(groups = "modify", dependsOnMethods = "replaceAttributes")
  public void removeAttributes(final String dn, final String attrs)
    throws Exception
  {
    final LdapEntry expected = convertStringToEntry(dn, attrs);
    final LdapEntry remove = convertStringToEntry(dn, attrs);

    final String[] attrsName = remove.getAttributeNames();
    remove.removeAttribute(attrsName[0]);
    expected.removeAttribute(attrsName[1]);

    final ConnectionFactory cf = createConnectionFactory();
    final ModifyOperation modify = new ModifyOperation(cf);
    final AttributeModification[] mods = new AttributeModification[expected.size()];
    int i = 0;
    for (LdapAttribute la : remove.getAttributes()) {
      mods[i++] = new AttributeModification(AttributeModification.Type.DELETE, la);
    }
    modify.execute(new ModifyRequest(dn, mods));

    final SearchOperation search = new SearchOperation(cf);
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, expected.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(expected);
  }
}
