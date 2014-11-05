/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link OidType}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3061 $ $Date: 2014-09-11 15:19:16 -0400 (Thu, 11 Sep 2014) $
 */
public class OidTypeTest
{


  /**
   * OID test data.
   *
   * @return  test data
   */
  @DataProvider(name = "oids")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new byte[] {
            (byte) 0x01,
          },
          "0.1",
        },
        // account
        new Object[] {
          new byte[] {
            (byte) 0x09, (byte) 0x92, (byte) 0x26, (byte) 0x89, (byte) 0x93,
            (byte) 0xF2, (byte) 0x2C, (byte) 0x64, (byte) 0x04, (byte) 0x05,
          },
          "0.9.2342.19200300.100.4.5",
        },
        // altServer
        new Object[] {
          new byte[] {
            (byte) 0x2B, (byte) 0x06, (byte) 0x01, (byte) 0x04, (byte) 0x01,
            (byte) 0x8B, (byte) 0x3A, (byte) 0x65, (byte) 0x78, (byte) 0x06,
          },
          "1.3.6.1.4.1.1466.101.120.6",
        },
        // aRecord
        new Object[] {
          new byte[] {
            (byte) 0x09, (byte) 0x92, (byte) 0x26, (byte) 0x89, (byte) 0x93,
            (byte) 0xF2, (byte) 0x2C, (byte) 0x64, (byte) 0x01, (byte) 0x1A,
          },
          "0.9.2342.19200300.100.1.26",
        },
        // associatedORAddress
        new Object[] {
          new byte[] {
            (byte) 0x2B, (byte) 0x06, (byte) 0x01, (byte) 0x04, (byte) 0x01,
            (byte) 0x83, (byte) 0x45, (byte) 0x07, (byte) 0x02, (byte) 0x06,
          },
          "1.3.6.1.4.1.453.7.2.6",
        },
        // attributeTypes
        new Object[] {
          new byte[] {
            (byte) 0x55, (byte) 0x15, (byte) 0x05,
          },
          "2.5.21.5",
        },
        // calCalURI
        new Object[] {
          new byte[] {
            (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xF7,
            (byte) 0x14, (byte) 0x01, (byte) 0x04, (byte) 0x83, (byte) 0x5E,
          },
          "1.2.840.113556.1.4.478",
        },
        // caseExactIA5Match
        new Object[] {
          new byte[] {
            (byte) 0x2B, (byte) 0x06, (byte) 0x01, (byte) 0x04, (byte) 0x01,
            (byte) 0x8B, (byte) 0x3A, (byte) 0x6D, (byte) 0x72, (byte) 0x01,
          },
          "1.3.6.1.4.1.1466.109.114.1",
        },
        // CN
        new Object[] {
          new byte[] {
            (byte) 0x55, (byte) 0x04, (byte) 0x03,
          },
          "2.5.4.3",
        },
        // distinguishedNameTableKey
        new Object[] {
          new byte[] {
            (byte) 0x2B, (byte) 0x06, (byte) 0x01, (byte) 0x04, (byte) 0x01,
            (byte) 0x83, (byte) 0x45, (byte) 0x07, (byte) 0x02, (byte) 0x03,
          },
          "1.3.6.1.4.1.453.7.2.3",
        },
      };
  }


  /**
   * OID test data.
   *
   * @return  test data
   */
  @DataProvider(name = "invalid-oids")
  public Object[][] createInvalidData()
  {
    return
      new Object[][] {
        new Object[] {null, },
        new Object[] {"", },
        new Object[] {"0", },
        new Object[] {"1", },
        new Object[] {"2", },
        new Object[] {"3.1", },
        new Object[] {"..1", },
        new Object[] {"192.168.1.1", },
        new Object[] {".123452", },
        new Object[] {"1.", },
        new Object[] {"1.345.23.34..234", },
        new Object[] {"1.345.23.34.234.", },
        new Object[] {".12.345.77.234", },
        new Object[] {".12.345.77.234.", },
        new Object[] {"1.2.3.4.A.5", },
        new Object[] {"1,2", },
      };
  }


  /**
   * @param  bytes  to decode.
   * @param  expected  oid to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"asn1"},
    dataProvider = "oids"
  )
  public void decode(final byte[] bytes, final String expected)
    throws Exception
  {
    Assert.assertEquals(OidType.decode(ByteBuffer.wrap(bytes)), expected);
  }


  /**
   * @param  expected  bytes to compare.
   * @param  oid  to encode.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"asn1"},
    dataProvider = "oids"
  )
  public void encode(final byte[] expected, final String oid)
    throws Exception
  {
    Assert.assertEquals(OidType.toBytes(OidType.parse(oid)), expected);
  }


  /**
   * @param  oid  to encode.
   */
  @Test(
    groups = {"asn1"},
    dataProvider = "invalid-oids"
  )
  public void invalid(final String oid)
  {
    try {
      new OidType(oid);
    } catch (Exception e) {
      AssertJUnit.assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
