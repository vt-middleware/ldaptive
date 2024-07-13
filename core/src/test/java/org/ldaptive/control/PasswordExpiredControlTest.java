/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link PasswordExpiredControl}.
 *
 * @author  Middleware Services
 */
public class PasswordExpiredControlTest
{


  /**
   * Password expired control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return new Object[][] {
      new Object[] {
        new DefaultDERBuffer(new byte[] {0x30}),
        new PasswordExpiredControl(),
      },
    };
  }


  /**
   * @param  berValue  to decode.
   * @param  control  password expired control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "response")
  public void decode(final DERBuffer berValue, final PasswordExpiredControl control)
    throws Exception
  {
    final PasswordExpiredControl actual = new PasswordExpiredControl(control.getCriticality());
    actual.decode(berValue);
    assertThat(actual).isEqualTo(control);
  }
}
