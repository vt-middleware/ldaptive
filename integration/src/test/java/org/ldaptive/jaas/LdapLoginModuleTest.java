/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.DnParser;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapLoginModule}.
 *
 * @author  Middleware Services
 */
public class LdapLoginModuleTest extends AbstractTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Entry created for auth tests. */
  private static LdapEntry testLdapEntry;

  /** Entries for group tests. */
  private static final Map<String, LdapEntry[]> GROUP_ENTRIES = new HashMap<>();

  static {
    // Initialize the map of group entries
    for (int i = 6; i <= 9; i++) {
      GROUP_ENTRIES.put(String.valueOf(i), new LdapEntry[2]);
    }
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry10")
  @BeforeClass(groups = {"jaas", "jaasInit"})
  public void createAuthEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);

    System.setProperty("java.security.auth.login.config", "target/test-classes/ldap_jaas.config");
  }


  /**
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createGroup6",
      "createGroup7",
      "createGroup8",
      "createGroup9"
    })
  @BeforeClass(groups = "jaas", dependsOnGroups = "jaasInit")
  public void createGroupEntry(
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9)
    throws Exception
  {
    // CheckStyle:Indentation OFF
    GROUP_ENTRIES.get("6")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile6)).getEntry();
    GROUP_ENTRIES.get("7")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile7)).getEntry();
    GROUP_ENTRIES.get("8")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile8)).getEntry();
    GROUP_ENTRIES.get("9")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile9)).getEntry();
    // CheckStyle:Indentation ON

    for (Map.Entry<String, LdapEntry[]> e : GROUP_ENTRIES.entrySet()) {
      super.createLdapEntry(e.getValue()[0]);
    }

    // setup group relationships
    final ModifyOperation modify = new ModifyOperation(TestUtils.createConnectionFactory());
    modify.execute(
      new ModifyRequest(
        GROUP_ENTRIES.get("6")[0].getDn(),
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute(
            "member",
            "cn=John Tyler," + DnParser.substring(testLdapEntry.getDn(), 1),
            "cn=Group 7," + DnParser.substring(testLdapEntry.getDn(), 1)))));
    modify.execute(
      new ModifyRequest(
        GROUP_ENTRIES.get("7")[0].getDn(),
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute(
            "member",
            "cn=Group 8," + DnParser.substring(testLdapEntry.getDn(), 1),
            "cn=Group 9," + DnParser.substring(testLdapEntry.getDn(), 1)))));
    modify.execute(
      new ModifyRequest(
        GROUP_ENTRIES.get("8")[0].getDn(),
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute("member", "cn=Group 7," + DnParser.substring(testLdapEntry.getDn(), 1)))));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "jaas")
  public void deleteAuthEntry()
    throws Exception
  {
    System.clearProperty("java.security.auth.login.config");

    super.deleteLdapEntry(testLdapEntry.getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("6")[0].getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("7")[0].getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("8")[0].getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("9")[0].getDn());

    try {
      PropertiesAuthenticatorFactory.close();
    } catch (UnsupportedOperationException e) {
      // ignore if not supported
      Assert.assertNotNull(e);
    }
    try {
      PropertiesRoleResolverFactory.close();
    } catch (UnsupportedOperationException e) {
      // ignore if not supported
      Assert.assertNotNull(e);
    }
    try {
      SpringAuthenticatorFactory.close();
    } catch (UnsupportedOperationException e) {
      // ignore if not supported
      Assert.assertNotNull(e);
    }
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void contextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive", dn, user, role, credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void contextSslTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-ssl", dn, user, role, credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void randomContextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-random", dn, user, role, credential, true);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasCredential" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void pooledDnResolverContextTest(final String dn, final String user, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-pooled-dnr", dn, user, "", credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasCredential" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void springPooledDnResolverContextTest(final String dn, final String user, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-pooled-dnr-spring", dn, user, "", credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined", "jaasCredential" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void rolesContextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-roles", dn, user, role, credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombinedRecursive", "jaasCredential" })
  @Test(groups = "jaas")
  public void rolesRecursiveContextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-roles-recursive", dn, user, role, credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRoleDefault", "jaasCredential" })
  @Test(groups = "jaas")
  public void useFirstContextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-use-first", dn, user, role, credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined", "jaasCredential" })
  @Test(groups = "jaas")
  public void tryFirstContextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-try-first", dn, user, role, credential, false);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasUserRole", "jaasCredential" })
  @Test(groups = "jaas")
  public void sufficientContextTest(final String dn, final String user, final String role, final String credential)
    throws Exception
  {
    doContextTest("ldaptive-sufficient", dn, user, role, credential, false);
  }


  /**
   * @param  name  of the jaas configuration
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   * @param  credential  to authenticate with.
   * @param  checkLdapDn  whether to check the LdapDnPrincipal
   *
   * @throws  Exception  On test failure.
   */
  private void doContextTest(
    final String name,
    final String dn,
    final String user,
    final String role,
    final String credential,
    final boolean checkLdapDn)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);
    callback.setPassword(INVALID_PASSWD);

    LoginContext lc = new LoginContext(name, callback);
    try {
      lc.login();
      Assert.fail("Invalid password, login should have failed");
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), LoginException.class);
    }

    callback.setPassword(credential);
    lc = new LoginContext(name, callback);
    try {
      lc.login();
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    final Set<LdapPrincipal> principals = lc.getSubject().getPrincipals(LdapPrincipal.class);
    Assert.assertEquals(principals.size(), 1);

    final LdapPrincipal p = principals.iterator().next();
    Assert.assertEquals(p.getName(), user);
    if (!"".equals(role)) {
      Assert.assertTrue(p.getLdapEntry().getAttributes().size() > 0, "Entry has no attributes: " + p.getLdapEntry());
    }

    final Set<LdapDnPrincipal> dnPrincipals = lc.getSubject().getPrincipals(LdapDnPrincipal.class);
    if (checkLdapDn) {
      Assert.assertEquals(dnPrincipals.size(), 1);

      final LdapDnPrincipal dnP = dnPrincipals.iterator().next();
      Assert.assertEquals(dnP.getName().toLowerCase(), dn.toLowerCase());
      if (!"".equals(role)) {
        Assert.assertTrue(
          dnP.getLdapEntry().getAttributes().size() > 0,
          "Entry has no attributes: " + p.getLdapEntry());
      }
    } else {
      Assert.assertEquals(dnPrincipals.size(), 0);
    }

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    String[] checkRoles = role.split("\\|");
    if (checkRoles.length == 1 && "".equals(checkRoles[0])) {
      checkRoles = new String[0];
    }
    Assert.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
          break;
        }
      }
      Assert.assertTrue(match);
    }

    final Set<LdapCredential> credentials = lc.getSubject().getPrivateCredentials(LdapCredential.class);
    Assert.assertEquals(credentials.size(), 1);

    final LdapCredential c = credentials.iterator().next();
    Assert.assertEquals(new String((char[]) c.getCredential()), credential);

    try {
      lc.logout();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(lc.getSubject().getPrincipals().size(), 0);
    Assert.assertEquals(lc.getSubject().getPrivateCredentials().size(), 0);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void rolesOnlyContextTest(final String dn, final String user, final String role)
    throws Exception
  {
    doRolesContextTest("ldaptive-roles-only", dn, user, role);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void dnRolesOnlyContextTest(final String dn, final String user, final String role)
    throws Exception
  {
    doRolesContextTest("ldaptive-dn-roles-only", dn, user, role);
  }


  /**
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "jaasDn", "jaasUser", "jaasRoleCombined" })
  @Test(
    groups = "jaas", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void dnRolesOnlyPooledContextTest(final String dn, final String user, final String role)
    throws Exception
  {
    doRolesContextTest("ldaptive-roles-only-pooled", dn, user, role);
  }


  /**
   * @param  name  of the jaas configuration
   * @param  dn  of this user
   * @param  user  to authenticate.
   * @param  role  to set for this user
   *
   * @throws  Exception  On test failure.
   */
  private void doRolesContextTest(final String name, final String dn, final String user, final String role)
    throws Exception
  {
    final TestCallbackHandler callback = new TestCallbackHandler();
    callback.setName(user);

    final LoginContext lc = new LoginContext(name, callback);
    try {
      lc.login();
    } catch (UnsupportedOperationException e) {
      throw e;
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    final Set<LdapRole> roles = lc.getSubject().getPrincipals(LdapRole.class);

    final Iterator<LdapRole> roleIter = roles.iterator();
    final String[] checkRoles = role.split("\\|");
    Assert.assertEquals(checkRoles.length, roles.size());
    while (roleIter.hasNext()) {
      final LdapRole r = roleIter.next();
      boolean match = false;
      for (String s : checkRoles) {
        if (s.equals(r.getName())) {
          match = true;
          break;
        }
      }
      Assert.assertTrue(match);
    }

    final Set<LdapGroup> roleGroups = lc.getSubject().getPrincipals(LdapGroup.class);
    Assert.assertEquals(roleGroups.size(), 2);
    for (LdapGroup g : roleGroups) {
      if ("Roles".equals(g.getName())) {
        final Set<Principal> members = g.getMembers();
        int count = 0;
        for (Principal p : members) {
          boolean match = false;
          for (LdapRole lr : lc.getSubject().getPrincipals(LdapRole.class)) {
            if (lr.getName().equals(p.getName())) {
              match = true;
            }
          }
          Assert.assertTrue(match);
          count++;
        }
        Assert.assertEquals(lc.getSubject().getPrincipals(LdapRole.class).size(), count);
      } else if ("Principals".equals(g.getName())) {
        final Set<Principal> members = g.getMembers();
        int count = 0;
        for (Principal p : members) {
          boolean match = false;
          for (LdapPrincipal lp : lc.getSubject().getPrincipals(LdapPrincipal.class)) {
            if (lp.getName().equals(p.getName())) {
              match = true;
            }
          }
          Assert.assertTrue(match);
          count++;
        }
        Assert.assertEquals(lc.getSubject().getPrincipals(LdapPrincipal.class).size(), count);
      } else {
        Assert.fail("Found invalid group");
      }
    }

    final Set<?> credentials = lc.getSubject().getPrivateCredentials();
    Assert.assertEquals(credentials.size(), 0);

    try {
      lc.logout();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    Assert.assertEquals(lc.getSubject().getPrincipals().size(), 0);
    Assert.assertEquals(lc.getSubject().getPrivateCredentials().size(), 0);
  }
}
