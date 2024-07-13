/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SearchOptionsControl}.
 *
 * @author  Middleware Services
 */
public class SearchOptionsControlTest
{


  /**
   * Search options control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // domain scope
        // BER: 30:03:02:01:00
        new Object[] {
          LdapUtils.base64Decode("MAMCAQA="),
          new SearchOptionsControl(),
        },
        // phantom root
        // BER: 30:03:02:01:01:
        new Object[] {
          LdapUtils.base64Decode("MAMCAQE="),
          new SearchOptionsControl(SearchOptionsControl.Flag.PHANTOM_ROOT),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  control  search options control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request")
  public void encode(final byte[] berValue, final SearchOptionsControl control)
    throws Exception
  {
    assertThat(control.encode()).isEqualTo(berValue);
  }
}
