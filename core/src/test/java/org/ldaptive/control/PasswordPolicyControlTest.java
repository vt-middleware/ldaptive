/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
    final PasswordPolicyControl timeBeforeExp = new PasswordPolicyControl(
      PasswordPolicyControl.WarningType.TIME_BEFORE_EXPIRATION, 2513067);

    final PasswordPolicyControl timeBeforeExpZero = new PasswordPolicyControl(
      PasswordPolicyControl.WarningType.TIME_BEFORE_EXPIRATION, 0);

    final PasswordPolicyControl graceAuthns = new PasswordPolicyControl(
      PasswordPolicyControl.WarningType.GRACE_AUTHNS_REMAINING, 4);

    final PasswordPolicyControl graceAuthnsZero = new PasswordPolicyControl(
      PasswordPolicyControl.WarningType.GRACE_AUTHNS_REMAINING, 0);

    final PasswordPolicyControl passExpired = new PasswordPolicyControl(PasswordPolicyControl.Error.PASSWORD_EXPIRED);

    final PasswordPolicyControl accountLocked = new PasswordPolicyControl(PasswordPolicyControl.Error.ACCOUNT_LOCKED);

    final PasswordPolicyControl changeAfterReset = new PasswordPolicyControl(
      PasswordPolicyControl.WarningType.TIME_BEFORE_EXPIRATION,
      2513067,
      PasswordPolicyControl.Error.CHANGE_AFTER_RESET);

    return
      new Object[][] {
        // timeBeforeExpiration is set
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x07, (byte) 0xA0, 0x05, (byte) 0x80, 0x03, 0x26, 0x58, (byte) 0xAB}),
          timeBeforeExp,
        },
        // timeBeforeExpiration is zero
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x05, (byte) 0xA0, 0x03, (byte) 0x80, 0x01, 0x00}),
          timeBeforeExpZero,
        },
        // graceAuthNsRemaining is set to 4
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x05, (byte) 0xA0, 0x03, (byte) 0x81, 0x01, 0x04}),
          graceAuthns,
        },
        // graceAuthNsRemaining is zero
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x05, (byte) 0xA0, 0x03, (byte) 0x81, 0x01, 0x00}),
          graceAuthnsZero,
        },
        // error=passwordExpired
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x03, (byte) 0x81, 0x01, 0x00}),
          passExpired,
        },
        // error=accountLocked
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x03, (byte) 0x81, 0x01, 0x01}),
          accountLocked,
        },
        // error=changeAfterReset and timeBeforeExpiration is set
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0A, (byte) 0xA0, 0x05, (byte) 0x80, 0x03, 0x26, 0x58, (byte) 0xAB, (byte) 0x81, 0x01, 0x02}),
          changeAfterReset,
        },
        // empty control
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x00}),
          new PasswordPolicyControl(),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  control  ppolicy control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "response")
  public void decode(final DERBuffer berValue, final PasswordPolicyControl control)
    throws Exception
  {
    final PasswordPolicyControl actual = new PasswordPolicyControl(control.getCriticality());
    actual.decode(berValue);
    assertThat(actual).isEqualTo(control);
  }
}
