/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.Credential;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PasswordModifyOperation}.
 *
 * @author  Middleware Services
 */
public class PasswordModifyOperationTest extends AbstractTest
{

  /** Invalid password test data. */
  public static final String INVALID_PASSWD = "not-a-password";

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry16")
  @BeforeClass(groups = {"extended"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"extended"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to modify.
   * @param  oldPass  to change.
   * @param  newPass  to change to.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "passwordModifyDn",
      "passwordModifyOldPass",
      "passwordModifyNewPass"
    }
  )
  @Test(groups = {"extended"})
  public void modify(
    final String dn,
    final String oldPass,
    final String newPass)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }
    final Authenticator auth = TestUtils.createSSLDnAuthenticator();
    AuthenticationResponse response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(oldPass)));
    AssertJUnit.assertTrue(response.getResult());

    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final PasswordModifyOperation modify = new PasswordModifyOperation(conn);
      // invalid password
      try {
        final Response<Credential> res = modify.execute(
          new PasswordModifyRequest(
            dn, new Credential(INVALID_PASSWD), new Credential(newPass)));
        AssertJUnit.assertEquals(
          ResultCode.UNWILLING_TO_PERFORM, res.getResultCode());
      } catch (LdapException e) {
        AssertJUnit.assertEquals(
          ResultCode.UNWILLING_TO_PERFORM, e.getResultCode());
      }

      // change password
      Response<Credential> modifyResponse = modify.execute(
        new PasswordModifyRequest(
          dn, new Credential(oldPass), new Credential(newPass)));
      AssertJUnit.assertNotNull(modifyResponse);
      AssertJUnit.assertNull(modifyResponse.getResult());
      response = auth.authenticate(
        new AuthenticationRequest(dn, new Credential(oldPass)));
      AssertJUnit.assertFalse(response.getResult());
      response = auth.authenticate(
        new AuthenticationRequest(dn, new Credential(newPass)));
      AssertJUnit.assertTrue(response.getResult());

      // generate password
      modifyResponse = modify.execute(new PasswordModifyRequest(dn));
      AssertJUnit.assertNotNull(modifyResponse);
      AssertJUnit.assertNotNull(modifyResponse.getResult());
      response = auth.authenticate(
        new AuthenticationRequest(dn, new Credential(newPass)));
      AssertJUnit.assertFalse(response.getResult());
      response = auth.authenticate(
        new AuthenticationRequest(dn, modifyResponse.getResult()));
      AssertJUnit.assertTrue(response.getResult());
    } finally {
      conn.close();
    }
  }
}
