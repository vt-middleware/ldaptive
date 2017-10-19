/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PasswordPolicyControl}.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyControlTest
{


  /**
   * Password policy control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    final PasswordPolicyControl timeBeforeExp = new PasswordPolicyControl();
    timeBeforeExp.setTimeBeforeExpiration(2513067);

    final PasswordPolicyControl timeBeforeExpZero = new PasswordPolicyControl();
    timeBeforeExpZero.setTimeBeforeExpiration(0);

    final PasswordPolicyControl graceAuthns = new PasswordPolicyControl();
    graceAuthns.setGraceAuthNsRemaining(4);

    final PasswordPolicyControl passExpired = new PasswordPolicyControl();
    passExpired.setError(PasswordPolicyControl.Error.PASSWORD_EXPIRED);

    final PasswordPolicyControl accountLocked = new PasswordPolicyControl();
    accountLocked.setError(PasswordPolicyControl.Error.ACCOUNT_LOCKED);

    return
      new Object[][] {
        // only timeBeforeExpiration is set
        // BER: 30:07:A0:05:80:03:26:58:AB
        new Object[] {
          LdapUtils.base64Decode("MAegBYADJlir"),
          timeBeforeExp,
        },
        // only timeBeforeExpiration is zero
        // BER: 30:05:A0:03:80:01:00
        new Object[] {
          LdapUtils.base64Decode("MAWgA4ABAA=="),
          timeBeforeExpZero,
        },
        // only graceAuthNsRemaining is set
        // BER: 30:05:A0:03:81:01:04
        new Object[] {
          LdapUtils.base64Decode("MAWgA4EBBA=="),
          graceAuthns,
        },
        // error=passwordExpired
        // BER: 30:03:81:01:00
        new Object[] {
          LdapUtils.base64Decode("MAOBAQA="),
          passExpired,
        },
        // error=accountLocked
        // BER: 30:03:81:01:01
        new Object[] {
          LdapUtils.base64Decode("MAOBAQE="),
          accountLocked,
        },
        // empty control
        // BER: 30:00
        new Object[] {
          LdapUtils.base64Decode("MAA="),
          new PasswordPolicyControl(),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  ppolicy control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "response")
  public void decode(final byte[] berValue, final PasswordPolicyControl expected)
    throws Exception
  {
    final PasswordPolicyControl actual = new PasswordPolicyControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
