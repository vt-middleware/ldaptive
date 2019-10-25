/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ActiveDirectoryAccountState}.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAccountStateTest
{


  /**
   * AD test data.
   *
   * @return  error messages
   */
  @DataProvider(name = "errors")
  public Object[][] createTestParams()
  {
    return
      new Object[][] {
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 525, v893",
          ActiveDirectoryAccountState.Error.NO_SUCH_USER,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 52e, v893",
          ActiveDirectoryAccountState.Error.LOGON_FAILURE,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09042F, comment: " +
            "AcceptSecurityContext error, data 52e, v2580\u0000",
          ActiveDirectoryAccountState.Error.LOGON_FAILURE,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 530, v893",
          ActiveDirectoryAccountState.Error.INVALID_LOGON_HOURS,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 531, v893",
          ActiveDirectoryAccountState.Error.INVALID_WORKSTATION,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 532, v893",
          ActiveDirectoryAccountState.Error.PASSWORD_EXPIRED,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 533, v893",
          ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 701, v893",
          ActiveDirectoryAccountState.Error.ACCOUNT_EXPIRED,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 773, v893",
          ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE,
        },
        new Object[] {
          "80090308: LdapErr: DSID-0C09030B, comment: " +
            "AcceptSecurityContext error, data 775, v893",
          ActiveDirectoryAccountState.Error.ACCOUNT_LOCKED_OUT,
        },
      };
  }


  /**
   * Tests message parsing.
   *
   * @param  message  error message
   * @param  err  corresponding error enum
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "auth-ext", dataProvider = "errors")
  public void parseMessage(final String message, final ActiveDirectoryAccountState.Error err)
    throws Exception
  {
    Assert.assertEquals(ActiveDirectoryAccountState.Error.parse(message), err);
  }
}
