/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link GetStatsControl}.
 *
 * @author  Middleware Services
 */
public class GetStatsControlTest
{


  /**
   * Get stats control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    final GetStatsControl ctrl = new GetStatsControl();
    ctrl.getStatistics().put("pagesReferenced", 45094);
    ctrl.getStatistics().put("index", "Ancestors_index:0:N;");
    ctrl.getStatistics().put("pagesRedirtied", 2);
    ctrl.getStatistics().put("entriesVisited", 5010);
    ctrl.getStatistics().put("logRecordCount", 0);
    ctrl.getStatistics().put("pagesDirtied", 0);
    ctrl.getStatistics().put("entriesReturned", 1);
    ctrl.getStatistics().put("callTime", 15);
    ctrl.getStatistics().put("logRecordBytes", 0);
    ctrl.getStatistics().put("threadCount", 1);
    ctrl.getStatistics().put("pagesPreread", 0);
    ctrl.getStatistics().put("pagesRead", 0);
    ctrl.getStatistics().put("filter", "(uid=2)");
    return
      new Object[][] {
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, (byte) 0x84, 0x00, 0x00, 0x00, 0x6E, 0x02, 0x01, 0x01, 0x02, 0x01, 0x01, 0x02, 0x01, 0x03, 0x02,
              0x01, 0x0F, 0x02, 0x01, 0x05, 0x02, 0x01, 0x01, 0x02, 0x01, 0x06, 0x02, 0x02, 0x13, (byte) 0x92, 0x02,
              0x01, 0x07, 0x04, 0x0A, 0x20, 0x28, 0x75, 0x69, 0x64, 0x3D, 0x32, 0x29, 0x20, 0x00, 0x02, 0x01, 0x08,
              0x04, 0x15, 0x41, 0x6E, 0x63, 0x65, 0x73, 0x74, 0x6F, 0x72, 0x73, 0x5F, 0x69, 0x6E, 0x64, 0x65, 0x78,
              0x3A, 0x30, 0x3A, 0x4E, 0x3B, 0x00, 0x02, 0x01, 0x09, 0x02, 0x03, 0x00, (byte) 0xB0, 0x26, 0x02, 0x01,
              0x0A, 0x02, 0x01, 0x00, 0x02, 0x01, 0x0B, 0x02, 0x01, 0x00, 0x02, 0x01, 0x0C, 0x02, 0x01, 0x00, 0x02,
              0x01, 0x0D, 0x02, 0x01, 0x02, 0x02, 0x01, 0x0E, 0x02, 0x01, 0x00, 0x02, 0x01, 0x0F, 0x02, 0x01, 0x00}),
          ctrl,
        },
      };
  }


  /**
   * @param  control  get stats control to test.
   * @param  berValue  to encode.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "response")
  public void decode(final DERBuffer berValue, final GetStatsControl control)
    throws Exception
  {
    final GetStatsControl actual = new GetStatsControl(control.getCriticality());
    actual.decode(berValue);
    assertThat(actual).isEqualTo(control);
  }
}
