/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.AuthorizationIdentityRequestControl;
import org.ldaptive.control.AuthorizationIdentityResponseControl;
import org.ldaptive.control.SessionTrackingControl;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link BindOperation}.
 *
 * @author  Middleware Services
 */
public class BindOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry32")
  @BeforeClass(groups = {"bind"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"bind"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to bind as.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("bindDn")
  @Test(groups = {"bind"})
  public void bindFailure(final String dn)
    throws Exception
  {
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final BindOperation bind = new BindOperation(conn);
      final Response<Void> response = bind.execute(new BindRequest(dn, new Credential("INVALID-PASSWD")));
      AssertJUnit.assertEquals(ResultCode.INVALID_CREDENTIALS, response.getResultCode());
    } catch (LdapException e) {
      AssertJUnit.assertEquals(ResultCode.INVALID_CREDENTIALS, e.getResultCode());
    }
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = {"bind"})
  public void bindSuccess(final String dn, final String passwd)
    throws Exception
  {
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final BindOperation bind = new BindOperation(conn);
      final Response<Void> response = bind.execute(new BindRequest(dn, new Credential(passwd)));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    }
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = {"bind"})
  public void bindIdentityControl(final String dn, final String passwd)
    throws Exception
  {
    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      throw new UnsupportedOperationException("Apache LDAP does not support this control");
    }

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final BindOperation bind = new BindOperation(conn);
      final BindRequest request = new BindRequest(dn, new Credential(passwd));
      request.setControls(new AuthorizationIdentityRequestControl());

      final Response<Void> response = bind.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());

      final AuthorizationIdentityResponseControl ctrl = (AuthorizationIdentityResponseControl) response.getControl(
        AuthorizationIdentityResponseControl.OID);
      if (ctrl == null) {
        throw new UnsupportedOperationException("Authorization Identity Control not supported");
      }
      AssertJUnit.assertEquals("dn:" + dn.toLowerCase(), ctrl.getAuthorizationId().toLowerCase());
    }
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = {"bind"})
  public void bindSessionTrackingControl(final String dn, final String passwd)
    throws Exception
  {
    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      throw new UnsupportedOperationException("Apache LDAP does not support this control");
    }

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final BindOperation bind = new BindOperation(conn);
      final BindRequest request = new BindRequest(dn, new Credential(passwd));
      request.setControls(
        new SessionTrackingControl(
          "151.101.32.133",
          "www.ldaptive.org",
          SessionTrackingControl.USERNAME_ACCT_OID,
          ""));

      final Response<Void> response = bind.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    }
  }
}
