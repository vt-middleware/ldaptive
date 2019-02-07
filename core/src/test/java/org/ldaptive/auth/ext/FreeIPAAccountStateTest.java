/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import org.ldaptive.ResultCode;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
          ResultCode.NO_SUCH_OBJECT,
          null,
          FreeIPAAccountState.Error.ACCOUNT_NOT_FOUND,
        },
        new Object[] {
          ResultCode.INVALID_CREDENTIALS,
          null,
          FreeIPAAccountState.Error.CREDENTIAL_NOT_FOUND,
        },
        new Object[] {
          ResultCode.INSUFFICIENT_ACCESS_RIGHTS,
          null,
          FreeIPAAccountState.Error.FAILED_AUTHENTICATION,
        },
        new Object[] {
          ResultCode.UNWILLING_TO_PERFORM,
          "Entry permanently locked.\n",
          FreeIPAAccountState.Error.LOGIN_LOCKOUT,
        },
        new Object[] {
          ResultCode.UNWILLING_TO_PERFORM,
          "Too many failed logins.\n",
          FreeIPAAccountState.Error.MAXIMUM_LOGINS_EXCEEDED,
        },
        new Object[] {
          ResultCode.UNWILLING_TO_PERFORM,
          "Account (Kerberos principal) is expired",
          FreeIPAAccountState.Error.ACCOUNT_EXPIRED,
        },
        new Object[] {
          ResultCode.UNWILLING_TO_PERFORM,
          "Account inactivated. Contact system administrator.",
          FreeIPAAccountState.Error.ACCOUNT_DISABLED,
        },
        new Object[] {
          ResultCode.OPERATIONS_ERROR,
          null,
          FreeIPAAccountState.Error.UNKNOWN,
        },
        new Object[] {
          ResultCode.SUCCESS,
          null,
          null,
        },
      };
  }


  /**
   * Tests message parsing.
   *
   * @param  rc  result code
   * @param  message  error message
   * @param  err  corresponding error enum
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "auth-ext", dataProvider = "errors")
  public void parse(final ResultCode rc, final String message, final FreeIPAAccountState.Error err)
    throws Exception
  {
    AssertJUnit.assertEquals(err, FreeIPAAccountState.Error.parse(rc, message));
  }
}
