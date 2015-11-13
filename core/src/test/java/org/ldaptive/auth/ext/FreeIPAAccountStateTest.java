/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import static org.ldaptive.ResultCode.*;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;

/**
 * Unit test for {@link FreeIPAAccountState}.
 *
 * @author  Middleware Services
 */
public class FreeIPAAccountStateTest
{

 /**
   * FreeIPA test data.
   *
   * @return  error messages
   */
  @DataProvider(name = "errors")
  public Object[][] createTestParams()
  {
    return
      new Object[][] {
        new Object[] {
          NO_SUCH_OBJECT,
          null,
          FreeIPAAccountState.Error.ACCOUNT_NOT_FOUND,
        },
        new Object[] {
          INVALID_CREDENTIALS,
          null,
          FreeIPAAccountState.Error.CREDENTIAL_NOT_FOUND,
        },
        new Object[] {
          INSUFFICIENT_ACCESS_RIGHTS,
          null,
          FreeIPAAccountState.Error.FAILED_AUTHENTICATION,
        },
        new Object[] {
          UNWILLING_TO_PERFORM,
          "Entry permanently locked.\n",
          FreeIPAAccountState.Error.LOGIN_LOCKOUT,
        },
        new Object[] {
          UNWILLING_TO_PERFORM,
          "Too many failed logins.\n",
          FreeIPAAccountState.Error.MAXIMUM_LOGINS_EXCEEDED,
        },
        new Object[] {
          UNWILLING_TO_PERFORM,
          "Account (Kerberos principal) is expired",
          FreeIPAAccountState.Error.ACCOUNT_EXPIRED,
        },
        new Object[] {
          UNWILLING_TO_PERFORM,
          "Account inactivated. Contact system administrator.",
          FreeIPAAccountState.Error.ACCOUNT_DISABLED,
        },
        new Object[] {
          OPERATIONS_ERROR,
          null,
          FreeIPAAccountState.Error.UNKNOWN,
        },
        new Object[] {
          SUCCESS,
          null,
          null,
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
  @Test(groups = {"auth-ext"}, dataProvider = "errors")
  public void parse(final ResultCode rc, final String message, final FreeIPAAccountState.Error err)
    throws Exception
  {
    AssertJUnit.assertEquals(err, FreeIPAAccountState.Error.parse(rc, message));
  }
}
