/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import java.util.EnumSet;
import org.ldaptive.LdapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link PersistentSearchRequestControl}.
 *
 * @author  Middleware Services
 */
public class PersistentSearchRequestControlTest
{


  /**
   * Persistent search request control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // all types, changesOnly=true, returnEcs=true
        // BER: 30:09:02:01:0F:01:01:FF:01:01:FF
        new Object[] {
          LdapUtils.base64Decode("MAkCAQ8BAf8BAf8="),
          new PersistentSearchRequestControl(EnumSet.allOf(PersistentSearchChangeType.class), true, true),
        },
        // modify type, changesOnly=false, returnEcs=true
        // BER: 30:09:02:01:04:01:01:00:01:01:FF
        new Object[] {
          LdapUtils.base64Decode("MAkCAQQBAQABAf8="),
          new PersistentSearchRequestControl(EnumSet.of(PersistentSearchChangeType.MODIFY), false, true),
        },
        // add and delete types, changesOnly=true, returnEcs=false
        // BER: 30:09:02:01:03:01:01:FF:01:01:00
        new Object[] {
          LdapUtils.base64Decode("MAkCAQMBAf8BAQA="),
          new PersistentSearchRequestControl(
            EnumSet.of(PersistentSearchChangeType.ADD, PersistentSearchChangeType.DELETE),
            true,
            false),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  control  persistent search request control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request")
  public void encode(final byte[] berValue, final PersistentSearchRequestControl control)
    throws Exception
  {
    assertThat(control.encode()).isEqualTo(berValue);
  }
}
