/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link IntegerType}.
 *
 * @author  Middleware Services
 */
public class IntegerTypeTest
{


  /**
   * Integer test data.
   *
   * @return  test data
   */
  @DataProvider(name = "ints")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new byte[] {(byte) 0x00, },
          new BigInteger("0"),
          new BigInteger("0"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, },
          new BigInteger("1"),
          new BigInteger("1"),
        },
        new Object[] {
          new byte[] {(byte) 0x7F, },
          new BigInteger("127"),
          new BigInteger("127"),
        },
        new Object[] {
          new byte[] {(byte) 0x80, },
          new BigInteger("-128"),
          new BigInteger("128"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, },
          new BigInteger("-1"),
          new BigInteger("255"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0x00, },
          new BigInteger("256"),
          new BigInteger("256"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0x7F, },
          new BigInteger("383"),
          new BigInteger("383"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0x80, },
          new BigInteger("384"),
          new BigInteger("384"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0xFF, },
          new BigInteger("511"),
          new BigInteger("511"),
        },
        new Object[] {
          new byte[] {(byte) 0x02, (byte) 0x00, },
          new BigInteger("512"),
          new BigInteger("512"),
        },
        new Object[] {
          new byte[] {(byte) 0x7F, (byte) 0x00, },
          new BigInteger("32512"),
          new BigInteger("32512"),
        },
        new Object[] {
          new byte[] {(byte) 0x7F, (byte) 0xFF, },
          new BigInteger("32767"),
          new BigInteger("32767"),
        },
        new Object[] {
          new byte[] {(byte) 0x80, (byte) 0x00, },
          new BigInteger("-32768"),
          new BigInteger("32768"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0x00, },
          new BigInteger("-256"),
          new BigInteger("65280"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0x7F, },
          new BigInteger("-129"),
          new BigInteger("65407"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0xFF, },
          new BigInteger("-1"),
          new BigInteger("65535"),
        },
      };
  }


  /**
   * @param  bytes  to decode.
   * @param  expected  int to compare.
   * @param  unsigned  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"asn1"}, dataProvider = "ints")
  public void decode(final byte[] bytes, final BigInteger expected, final BigInteger unsigned)
    throws Exception
  {
    Assert.assertEquals(IntegerType.decode(ByteBuffer.wrap(bytes)), expected);
  }


  /**
   * @param  bytes  to decode.
   * @param  expected  int to compare.
   * @param  unsigned  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"asn1"}, dataProvider = "ints")
  public void decodeUnsigned(final byte[] bytes, final BigInteger expected, final BigInteger unsigned)
    throws Exception
  {
    Assert.assertEquals(IntegerType.decodeUnsigned(ByteBuffer.wrap(bytes)), unsigned);
  }


  /**
   * @param  expected  bytes to compare.
   * @param  integer  to encode.
   * @param  unsigned  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"asn1"}, dataProvider = "ints")
  public void encode(final byte[] expected, final BigInteger integer, final BigInteger unsigned)
    throws Exception
  {
    if (integer.intValue() == -1 && expected.length > 1) {
      // ignore multi byte negative 1
      return;
    } else {
      Assert.assertEquals(IntegerType.toBytes(integer), expected);
    }
  }
}
