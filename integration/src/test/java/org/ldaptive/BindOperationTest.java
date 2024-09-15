/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import org.ldaptive.control.AuthorizationIdentityRequestControl;
import org.ldaptive.control.AuthorizationIdentityResponseControl;
import org.ldaptive.control.SessionTrackingControl;
import org.ldaptive.dn.Dn;
import org.ldaptive.handler.ResultPredicate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
  @BeforeClass(groups = "bind")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "bind")
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
  @Test(groups = "bind")
  public void simpleBindStringFailure(final String dn)
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    final BindResponse response = bind.execute(new SimpleBindRequest(dn, "INVALID-PASSWD"));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.INVALID_CREDENTIALS);

    try {
      bind.setThrowCondition(ResultPredicate.NOT_SUCCESS);
      bind.execute(new SimpleBindRequest(dn, "INVALID-PASSWD"));
    } catch (LdapException e) {
      assertThat(e.getResultCode()).isEqualTo(ResultCode.INVALID_CREDENTIALS);
    }
  }


  /**
   * @param  dn  to bind as.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("bindDn")
  @Test(groups = "bind")
  public void simpleBindCredentialFailure(final String dn)
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    BindResponse response = bind.execute(
      new SimpleBindRequest(dn, new Credential("INVALID-PASSWD")));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.INVALID_CREDENTIALS);
    response = bind.execute(
      new SimpleBindRequest(dn, new Credential("INVALID-PASSWD".toCharArray())));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.INVALID_CREDENTIALS);
    response = bind.execute(
      new SimpleBindRequest(dn, new Credential("INVALID-PASSWD".getBytes(StandardCharsets.UTF_8))));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.INVALID_CREDENTIALS);
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = "bind")
  public void simpleBindStringSuccess(final String dn, final String passwd)
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    final BindResponse response = bind.execute(new SimpleBindRequest(dn, passwd));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = "bind")
  public void simpleBindCredentialSuccess(final String dn, final String passwd)
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    BindResponse response = bind.execute(new SimpleBindRequest(dn, new Credential(passwd)));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    response = bind.execute(new SimpleBindRequest(dn, new Credential(passwd.toCharArray())));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    response = bind.execute(new SimpleBindRequest(dn, new Credential(passwd.getBytes(StandardCharsets.UTF_8))));
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = "bind")
  public void bindIdentityControl(final String dn, final String passwd)
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    final SimpleBindRequest request = new SimpleBindRequest(dn, passwd);
    request.setControls(new AuthorizationIdentityRequestControl());

    final BindResponse response = bind.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);

    final AuthorizationIdentityResponseControl ctrl = (AuthorizationIdentityResponseControl) response.getControl(
      AuthorizationIdentityResponseControl.OID);
    if (ctrl == null) {
      throw new UnsupportedOperationException("Authorization Identity Control not supported");
    }
    assertThat(new Dn(ctrl.getAuthorizationId()).format()).isEqualTo("dn:" + new Dn(dn).format());
  }


  /**
   * @param  dn  to bind as.
   * @param  passwd  to bind with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "bindDn", "bindPasswd" })
  @Test(groups = "bind")
  public void bindSessionTrackingControl(final String dn, final String passwd)
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    final SimpleBindRequest request = new SimpleBindRequest(dn, passwd);
    request.setControls(
      new SessionTrackingControl(
        "151.101.32.133",
        "www.ldaptive.org",
        SessionTrackingControl.USERNAME_ACCT_OID,
        ""));

    final BindResponse response = bind.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "bind")
  public void bindAnonymous()
    throws Exception
  {
    final BindOperation bind = new BindOperation(TestUtils.createConnectionFactory());
    final AnonymousBindRequest request = new AnonymousBindRequest();
    final BindResponse response = bind.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
  }
}
