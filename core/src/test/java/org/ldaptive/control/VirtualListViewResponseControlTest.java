/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.ResultCode;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link VirtualListViewResponseControl}.
 *
 * @author  Middleware Services
 */
public class VirtualListViewResponseControlTest
{


  /**
   * Virtual list view response control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // position=1, count=59, result=success, context not null
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x13, 0x02, 0x01, 0x01, 0x02, 0x01, 0x3B, 0x0A, 0x01, 0x00, 0x04, 0x08, (byte) 0x80, 0x28, 0x7D,
              0x08, 0x00, 0x00, 0x00, 0x00}),
          new VirtualListViewResponseControl(
            1,
            59,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x80,
              (byte) 0x28,
              (byte) 0x7D,
              (byte) 0x08,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
        // position=10, count=55, result=success, context not null
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x13, 0x02, 0x01, 0x0A, 0x02, 0x01, 0x37, 0x0A, 0x01, 0x00, 0x04, 0x08, 0x00, (byte) 0x9A,
              (byte) 0x96, 0x01, 0x00, 0x00, 0x00, 0x00}),
          new VirtualListViewResponseControl(
            10,
            55,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x00,
              (byte) 0x9A,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
        // position=12, count=55, result=success, context not null
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x13, 0x02, 0x01, 0x0C, 0x02, 0x01, 0x37, 0x0A, 0x01, 0x00, 0x04, 0x08, (byte) 0x80, (byte) 0x99,
              (byte) 0x96, 0x01, 0x00, 0x00, 0x00, 0x00}),
          new VirtualListViewResponseControl(
            12,
            55,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x80,
              (byte) 0x99,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
        // position=22, count=55, result=success, context not null
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x13, 0x02, 0x01, 0x16, 0x02, 0x01, 0x37, 0x0A, 0x01, 0x00, 0x04, 0x08, (byte) 0x80, (byte) 0x99,
              (byte) 0x96, 0x01, 0x00, 0x00, 0x00, 0x00}),
          new VirtualListViewResponseControl(
            22,
            55,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x80,
              (byte) 0x99,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  control  virtual list view response control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "response")
  public void decode(final DERBuffer berValue, final VirtualListViewResponseControl control)
    throws Exception
  {
    final VirtualListViewResponseControl actual = new VirtualListViewResponseControl(control.getCriticality());
    actual.decode(berValue);
    assertThat(actual).isEqualTo(control);
  }
}
