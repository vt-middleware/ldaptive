/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.util.Arrays;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link DERParser} class.
 *
 * @author  Middleware Services
 */
public class DERParserTest
{


  /**
   * DER parser test data.
   *
   * @return  der parser data
   */
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "parserData")
  public Object[][] createBufferData()
  {
    return
      new Object[][] {
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {0x30, 0x0B, 0x04, 0x09, 0x31, 0x39, 0x32, 0x2e, 0x30, 0x2e, 0x32, 0x2e, 0x31}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ", 1),
            new TestParseHandler("/SEQ/OCTSTR", 1),
          },
        },
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x09,
              (byte) 0xA0, 0x04,
              (byte) 0x80, 0x02, 0x30, 0x39,
              (byte) 0x81, 0x01, 0x02}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/CTX(0)/CTX(0)", 1),
            new TestParseHandler("/SEQ/CTX(0)/CTX(1)", 0),
            new TestParseHandler("/SEQ/CTX(1)", 1),
          },
        },
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0C,
              (byte) 0xA0, 0x07,
              (byte) 0x80, 0x02, 0x30, 0x39,
              (byte) 0x81, 0x01, 0x04,
              (byte) 0x81, 0x01, 0x02}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/CTX(0)/CTX(0)", 1),
            new TestParseHandler("/SEQ/CTX(0)/CTX(1)", 1),
            new TestParseHandler("/SEQ/CTX(1)", 1),
          },
        },
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x05,
              0x02, 0x01, 0x00,
              0x04, 0x00}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/OCTSTR[1]", 1),
          },
        },
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0D,
              0x02, 0x01, 0x00,
              0x04, 0x08, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
              (byte) 0xFF}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/OCTSTR[1]", 1),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x0C, 0x02, 0x01, 0x01,
            0x61, 0x07,
            0x0a, 0x01, 0x00,
            0x04, 0x00,
            0x04, 0x00}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(1)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(1)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(1)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(1)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/APP(1)/CTX(7)", 0),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x30, 0x02, 0x01, 0x01,
            0x61, 0x2B,
            0x0A, 0x01, 0x0E,
            0x04, 0x00,
            0x04, 0x00,
            (byte) 0x87, 0x22, 0x3C, 0x31, 0x30, 0x61, 0x31, 0x33, 0x63, 0x37, 0x62, 0x66, 0x37, 0x30, 0x38, 0x63, 0x61,
            0x30, 0x66, 0x33, 0x39, 0x39, 0x63, 0x61, 0x39, 0x39, 0x65, 0x39, 0x32, 0x37, 0x64, 0x61, 0x38, 0x38, 0x62,
            0x3E}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(1)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(1)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(1)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(1)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/APP(1)/CTX(7)", 1),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x09, 0x02, 0x01, 0x04,
            0x64, 0x04,
            0x04, 0x00,
            0x30, 0x00}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(4)/OCTSTR[0]", 1),
            new TestParseHandler("/SEQ/APP(4)/SEQ/SEQ", 0),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x49, 0x02, 0x01, 0x02,
            0x64, 0x44,
            0x04, 0x11, 0x64, 0x63, 0x3D, 0x65, 0x78, 0x61, 0x6D, 0x70, 0x6C, 0x65, 0x2C, 0x64, 0x63, 0x3D, 0x63, 0x6F,
            0x6D,
            0x30, 0x2F,
            0x30, 0x1C,
            0x04, 0x0B, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x43, 0x6C, 0x61, 0x73, 0x73,
            0x31, 0x0D, 0x04, 0x03, 0x74, 0x6F, 0x70, 0x04, 0x06, 0x64, 0x6F, 0x6D, 0x61, 0x69, 0x6E,
            0x30, 0x0F,
            0x04, 0x02, 0x64, 0x63,
            0x31, 0x09, 0x04, 0x07, 0x65, 0x78, 0x61, 0x6D, 0x70, 0x6C, 0x65}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(4)/OCTSTR[0]", 1),
            new TestParseHandler("/SEQ/APP(4)/SEQ/SEQ", 2),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, (byte) 0x82, 0x01, 0x3D, 0x02, 0x01, 0x02,
            0x64, (byte) 0x82, 0x01, 0x36,
            0x04, 0x28, 0x63, 0x6E, 0x3D, 0x47, 0x72, 0x6F, 0x76, 0x65, 0x72, 0x20, 0x43, 0x6C, 0x65, 0x76, 0x65, 0x6C,
            0x61, 0x6E, 0x64, 0x2C, 0x6F, 0x75, 0x3D, 0x74, 0x65, 0x73, 0x74, 0x2C, 0x64, 0x63, 0x3D, 0x76, 0x74, 0x2C,
            0x64, 0x63, 0x3D, 0x65, 0x64, 0x75,
            0x30, (byte) 0x82, 0x01, 0x08,
            0x30, 0x18, 0x04, 0x02, 0x63, 0x6E,
            0x31, 0x12, 0x04, 0x10, 0x47, 0x72, 0x6F, 0x76, 0x65, 0x72, 0x20, 0x43, 0x6C, 0x65, 0x76, 0x65, 0x6C, 0x61,
            0x6E, 0x64,
            0x30, 0x1A, 0x04, 0x10, 0x64, 0x65, 0x70, 0x61, 0x72, 0x74, 0x6D, 0x65, 0x6E, 0x74, 0x4E, 0x75, 0x6D, 0x62,
            0x65, 0x72,
            0x31, 0x06, 0x04, 0x04, 0x30, 0x38, 0x34, 0x32,
            0x30, 0x21, 0x04, 0x0B, 0x64, 0x69, 0x73, 0x70, 0x6C, 0x61, 0x79, 0x4E, 0x61, 0x6D, 0x65,
            0x31, 0x12, 0x04, 0x10, 0x47, 0x72, 0x6F, 0x76, 0x65, 0x72, 0x20, 0x43, 0x6C, 0x65, 0x76, 0x65, 0x6C, 0x61,
            0x6E, 0x64,
            0x30, 0x15, 0x04, 0x09, 0x67, 0x69, 0x76, 0x65, 0x6E, 0x4E, 0x61, 0x6D, 0x65,
            0x31, 0x08, 0x04, 0x06, 0x47, 0x72, 0x6F, 0x76, 0x65, 0x72,
            0x30, 0x10, 0x04, 0x08, 0x69, 0x6E, 0x69, 0x74, 0x69, 0x61, 0x6C, 0x73,
            0x31, 0x04, 0x04, 0x02, 0x47, 0x43,
            0x30, 0x21, 0x04, 0x04, 0x6D, 0x61, 0x69, 0x6C,
            0x31, 0x19, 0x04, 0x17, 0x67, 0x63, 0x6C, 0x65, 0x76, 0x65, 0x6C, 0x61, 0x6E, 0x64, 0x40, 0x6C, 0x64, 0x61,
            0x70, 0x74, 0x69, 0x76, 0x65, 0x2E, 0x6F, 0x72, 0x67,
            0x30, 0x41, 0x04, 0x0B, 0x6F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x43, 0x6C, 0x61, 0x73, 0x73,
            0x31, 0x32,
            0x04, 0x0D, 0x69, 0x6E, 0x65, 0x74, 0x4F, 0x72, 0x67, 0x50, 0x65, 0x72, 0x73, 0x6F, 0x6E,
            0x04, 0x14, 0x6F, 0x72, 0x67, 0x61, 0x6E, 0x69, 0x7A, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x50, 0x65,
            0x72, 0x73, 0x6F, 0x6E,
            0x04, 0x06, 0x70, 0x65, 0x72, 0x73, 0x6F, 0x6E,
            0x04, 0x03, 0x74, 0x6F, 0x70,
            0x30, 0x11, 0x04, 0x02, 0x73, 0x6E,
            0x31, 0x0B, 0x04, 0x09, 0x43, 0x6C, 0x65, 0x76, 0x65, 0x6C, 0x61, 0x6E, 0x64,
            0x30, 0x0B, 0x04, 0x03, 0x75, 0x69, 0x64,
            0x31, 0x04, 0x04, 0x02, 0x32, 0x32}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(4)/OCTSTR[0]", 1),
            new TestParseHandler("/SEQ/APP(4)/SEQ/SEQ", 9),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x39, 0x02, 0x01, 0x02,
            0x65, 0x07, 0x0A, 0x01, 0x00,
            0x04, 0x00,
            0x04, 0x00,
            (byte) 0xA0, 0x2B,
            0x30, 0x29,
            0x04, 0x16, 0x31, 0x2E, 0x32, 0x2E, 0x38, 0x34, 0x30, 0x2E, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2E, 0x31,
            0x2E, 0x34, 0x2E, 0x33, 0x31, 0x39,
            0x04, 0x0F, 0x30, 0x0D, 0x02, 0x01, 0x00, 0x04, 0x08, 0x69, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(5)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 1),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x0C, 0x02, 0x01, 0x01,
            0x65, 0x07,
            0x0A, 0x01, 0x00,
            0x04, 0x00,
            0x04, 0x00}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(5)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x65, 0x02, 0x01, 0x02,
            0x65, 0x07,
            0x0A, 0x01, 0x00,
            0x04, 0x00,
            0x04, 0x00,
            (byte) 0xA0, 0x57,
            0x30, 0x22,
            0x04, 0x16, 0x31, 0x2E, 0x32, 0x2E, 0x38, 0x34, 0x30, 0x2E, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2E, 0x31,
            0x2E, 0x34, 0x2E, 0x34, 0x37, 0x34,
            0x01, 0x01, (byte) 0xFF,
            0x04, 0x05, 0x30, 0x03, 0x0A, 0x01, 0x00,
            0x30, 0x31,
            0x04, 0x18, 0x32, 0x2E, 0x31, 0x36, 0x2E, 0x38, 0x34, 0x30, 0x2E, 0x31, 0x2E, 0x31, 0x31, 0x33, 0x37, 0x33,
            0x30, 0x2E, 0x33, 0x2E, 0x34, 0x2E, 0x31, 0x30,
            0x04, 0x15, 0x30, 0x13, 0x02, 0x01, 0x03, 0x02, 0x01, 0x04, 0x0A, 0x01, 0x00, 0x04, 0x08, 0x50, 0x3D, 0x16,
            (byte) 0xEC, 0x13, 0x7F, 0x00, 0x00}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(5)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 2),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x6F, 0x02, 0x01, 0x02,
            0x65, 0x6A,
            0x0A, 0x01, 0x0A,
            0x04, 0x19, 0x6F, 0x75, 0x3D, 0x72, 0x65, 0x66, 0x65, 0x72, 0x72, 0x61, 0x6C, 0x73, 0x2C, 0x64, 0x63, 0x3D,
            0x76, 0x74, 0x2C, 0x64, 0x63, 0x3D, 0x65, 0x64, 0x75,
            0x04, 0x00,
            (byte) 0xA3, 0x48, 0x04, 0x46, 0x6C, 0x64, 0x61, 0x70, 0x3A, 0x2F, 0x2F, 0x6C, 0x64, 0x61, 0x70, 0x2D, 0x74,
            0x65, 0x73, 0x74, 0x2D, 0x31, 0x2E, 0x6D, 0x69, 0x64, 0x64, 0x6C, 0x65, 0x77, 0x61, 0x72, 0x65, 0x2E, 0x76,
            0x74, 0x2E, 0x65, 0x64, 0x75, 0x3A, 0x31, 0x30, 0x33, 0x38, 0x39, 0x2F, 0x6F, 0x75, 0x3D, 0x70, 0x65, 0x6F,
            0x70, 0x6C, 0x65, 0x2C, 0x64, 0x63, 0x3D, 0x76, 0x74, 0x2C, 0x64, 0x63, 0x3D, 0x65, 0x64, 0x75, 0x3F, 0x3F,
            0x6F, 0x6E, 0x65}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(5)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(5)/CTX(3)/OCTSTR[0]", 1),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x6D, 0x02, 0x01, 0x02,
            0x73, 0x68,
            0x04, 0x32, 0x6C, 0x64, 0x61, 0x70, 0x3A, 0x2F, 0x2F, 0x64, 0x73, 0x31, 0x2E, 0x65, 0x78, 0x61, 0x6D, 0x70,
            0x6C, 0x65, 0x2E, 0x63, 0x6F, 0x6D, 0x3A, 0x33, 0x38, 0x39, 0x2F, 0x64, 0x63, 0x3D, 0x65, 0x78, 0x61, 0x6D,
            0x70, 0x6C, 0x65, 0x2C, 0x64, 0x63, 0x3D, 0x63, 0x6F, 0x6D, 0x3F, 0x3F, 0x73, 0x75, 0x62, 0x3F,
            0x04, 0x32, 0x6C, 0x64, 0x61, 0x70, 0x3A, 0x2F, 0x2F, 0x64, 0x73, 0x32, 0x2E, 0x65, 0x78, 0x61, 0x6D, 0x70,
            0x6C, 0x65, 0x2E, 0x63, 0x6F, 0x6D, 0x3A, 0x33, 0x38, 0x39, 0x2F, 0x64, 0x63, 0x3D, 0x65, 0x78, 0x61, 0x6D,
            0x70, 0x6C, 0x65, 0x2C, 0x64, 0x63, 0x3D, 0x63, 0x6F, 0x6D, 0x3F, 0x3F, 0x73, 0x75, 0x62, 0x3F}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(19)/OCTSTR", 2),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x2C, 0x02, 0x01, 0x02,
            0x78, 0x27,
            0x0A, 0x01, 0x35,
            0x04, 0x00,
            0x04, 0x20, 0x75, 0x6E, 0x77, 0x69, 0x6C, 0x6C, 0x69, 0x6E, 0x67, 0x20, 0x74, 0x6F, 0x20, 0x76, 0x65, 0x72,
            0x69, 0x66, 0x79, 0x20, 0x6F, 0x6C, 0x64, 0x20, 0x70, 0x61, 0x73, 0x73, 0x77, 0x6F, 0x72, 0x64}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(24)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(24)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(24)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(24)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/APP(24)/CTX(10)", 0),
            new TestParseHandler("/SEQ/APP(24)/CTX(11)", 0),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x1A, 0x02, 0x01, 0x04,
            0x78, 0x15,
            0x0A, 0x01, 0x00,
            0x04, 0x00,
            0x04, 0x00,
            (byte) 0x8B, 0x0C, 0x30, 0x0A, (byte) 0x80, 0x08, 0x43, 0x6D, 0x33, 0x47, 0x6B, 0x79, 0x44, 0x61}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(24)/ENUM[0]", 1),
            new TestParseHandler("/SEQ/APP(24)/OCTSTR[1]", 1),
            new TestParseHandler("/SEQ/APP(24)/OCTSTR[2]", 1),
            new TestParseHandler("/SEQ/APP(24)/CTX(3)/OCTSTR[0]", 0),
            new TestParseHandler("/SEQ/APP(24)/CTX(10)", 0),
            new TestParseHandler("/SEQ/APP(24)/CTX(11)", 1),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {
            0x30, 0x2C, 0x02, 0x01, 0x02,
            0x79, 0x27,
            (byte) 0x80, 0x18, 0x31, 0x2E, 0x33, 0x2E, 0x36, 0x2E, 0x31, 0x2E, 0x34, 0x2E, 0x31, 0x2E, 0x34, 0x32, 0x30,
            0x33, 0x2E, 0x31, 0x2E, 0x39, 0x2E, 0x31, 0x2E, 0x34,
            (byte) 0x81, 0x0B, (byte) 0x80, 0x09, 0x4E, 0x6F, 0x6D, 0x4E, 0x6F, 0x6D, 0x4E, 0x6F, 0x6D}),
          new TestParseHandler[] {
            new TestParseHandler("/SEQ/INT[0]", 1),
            new TestParseHandler("/SEQ/APP(25)/CTX(0)", 1),
            new TestParseHandler("/SEQ/APP(25)/CTX(1)", 1),
            new TestParseHandler("/SEQ/CTX(0)/SEQ", 0),
          },
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * DER parser test data.
   *
   * @return  der parser data
   */
  @DataProvider(name = "invalid-data")
  public Object[][] invalidBufferData()
  {
    return
      new Object[][] {
        new Object[] {
          // unknown tag
          new DefaultDERBuffer(
            new byte[] {(byte) 0xFF, 0x00}),
        },
        new Object[] {
          // incorrect length at /SEQ
          new DefaultDERBuffer(
            new byte[] {0x30, 0x0F, 0x04, 0x09, 0x31, 0x39, 0x32, 0x2e, 0x30, 0x2e, 0x32, 0x2e, 0x31}),
        },
        new Object[] {
          // incorrect length at /SEQ/OCTSTR
          new DefaultDERBuffer(
            new byte[] {0x30, 0x0B, 0x04, 0x0F, 0x31, 0x39, 0x32, 0x2e, 0x30, 0x2e, 0x32, 0x2e, 0x31}),
        },
        new Object[] {
          // incorrect length at /SEQ/CTX(0)
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0C,
              (byte) 0xA0, 0x08,
              (byte) 0x80, 0x02, 0x30, 0x39,
              (byte) 0x81, 0x01, 0x04,
              (byte) 0x81, 0x01, 0x02}),
        },
        new Object[] {
          // incorrect length at /SEQ/CTX(0)
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0C,
              (byte) 0xA0, 0x0C,
              (byte) 0x80, 0x02, 0x30, 0x39,
              (byte) 0x81, 0x01, 0x04,
              (byte) 0x81, 0x01, 0x02}),
        },
        new Object[] {
          // incorrect length at /SEQ/CTX(0)/CTX(0)
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0C,
              (byte) 0xA0, 0x07,
              (byte) 0x80, 0x03, 0x30, 0x39,
              (byte) 0x81, 0x01, 0x04,
              (byte) 0x81, 0x01, 0x02}),
        },
        new Object[] {
          // incorrect length at /SEQ/CTX(0)/CTX(1)
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0C,
              (byte) 0xA0, 0x07,
              (byte) 0x80, 0x02, 0x30, 0x39,
              (byte) 0x81, 0x02, 0x04,
              (byte) 0x81, 0x01, 0x02}),
        },
        new Object[] {
          // incorrect length at /SEQ/CTX(1)
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x0C,
              (byte) 0xA0, 0x07,
              (byte) 0x80, 0x02, 0x30, 0x39,
              (byte) 0x81, 0x01, 0x04,
              (byte) 0x81, 0x02, 0x02}),
        },
        new Object[] {
          // max permutations (17) exceeded
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x1F, 0x30, 0x1D, 0x30, 0x1B, 0x30, 0x19, 0x30, 0x17, 0x30, 0x15, 0x30, 0x13, 0x30, 0x11,
              0x30, 0x0F, 0x30, 0x0D, 0x30, 0x0B, 0x30, 0x09, 0x30, 0x07, 0x30, 0x05, 0x30, 0x03, 0x30, 0x01,
              0x30, 0x00}),
        },
      };
  }


  /**
   * @param  encoded  to test
   * @param  handlers  to test
   */
  @Test(groups = "asn1", dataProvider = "parserData")
  public void testParse(final DERBuffer encoded, final TestParseHandler[] handlers)
  {

    final DERParser parser = new DERParser();
    for (TestParseHandler handler : handlers) {
      parser.registerHandler(new DERPath(handler.getDerPath()), handler);
    }
    parser.parse(encoded);
    Arrays.stream(handlers)
      .forEach(h -> assertThat(h.getActualCount())
        .withFailMessage("Path %s expected %s but was %s", h.getDerPath(), h.getExpectedCount(), h.getActualCount())
        .isEqualTo(h.getExpectedCount()));
  }


  /**
   * @param  encoded  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "invalid-data")
  public void testInvalidParse(final DERBuffer encoded)
    throws Exception
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(new DERPath(), new TestParseHandler("", 0));
    try {
      parser.parse(encoded);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }


  /** Parse handler used for testing. **/
  private static class TestParseHandler implements ParseHandler
  {

    /** DER path. */
    private final String derPath;

    /** number of times this handler should be invoked. */
    private final int expectedCount;

    /** number of times this handler was invoked. */
    private int actualCount;


    /**
     * Creates a new test parse handler.
     *
     * @param  path  DER path
     * @param  count  number of times this hander should be invoked
     */
    TestParseHandler(final String path, final int count)
    {
      derPath = path;
      expectedCount = count;
    }


    @Override
    public void handle(final DERParser parser, final DERBuffer encoded)
    {
      encoded.getRemainingBytes();
      actualCount++;
    }


    /**
     * Returns the DER path.
     *
     * @return  DER path
     */
    public String getDerPath()
    {
      return derPath;
    }


    /**
     * Returns the expected count.
     *
     * @return  expected count
     */
    public int getExpectedCount()
    {
      return expectedCount;
    }


    /**
     * Returns the actual count.
     *
     * @return  actual count
     */
    public int getActualCount()
    {
      return actualCount;
    }
  }
}