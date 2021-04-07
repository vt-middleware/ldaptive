/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractTest;
import org.ldaptive.Credential;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for the password modify extended operation.
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
  @BeforeClass(groups = "extended")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "extended")
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
  @Parameters({"passwordModifyDn", "passwordModifyOldPass", "passwordModifyNewPass"})
  @Test(groups = "extended")
  public void modify(final String dn, final String oldPass, final String newPass)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final Authenticator auth = TestUtils.createSSLDnAuthenticator();
    AuthenticationResponse response = auth.authenticate(new AuthenticationRequest(dn, new Credential(oldPass)));
    Assert.assertTrue(response.isSuccess());

    final ExtendedOperation modify = new ExtendedOperation(TestUtils.createConnectionFactory());
    // invalid password
    final ExtendedResponse res = modify.execute(new PasswordModifyRequest(dn, INVALID_PASSWD, newPass));
    Assert.assertEquals(res.getResultCode(), ResultCode.UNWILLING_TO_PERFORM);

    // change password
    ExtendedResponse modifyResponse = modify.execute(new PasswordModifyRequest(dn, oldPass, newPass));
    Assert.assertNotNull(modifyResponse);
    Assert.assertNull(modifyResponse.getResponseValue());
    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(oldPass)));
    Assert.assertFalse(response.isSuccess());
    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(newPass)));
    Assert.assertTrue(response.isSuccess());

    // generate password
    modifyResponse = modify.execute(new PasswordModifyRequest(dn));
    Assert.assertNotNull(modifyResponse);
    Assert.assertNotNull(modifyResponse.getResponseValue());
    response = auth.authenticate(new AuthenticationRequest(dn, new Credential(newPass)));
    Assert.assertFalse(response.isSuccess());
    response = auth.authenticate(
      new AuthenticationRequest(dn, new Credential(PasswordModifyResponseParser.parse(modifyResponse))));
    Assert.assertTrue(response.isSuccess());
  }
}
