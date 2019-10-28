/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.math.BigInteger;
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
          Integer.parseUnsignedInt("0"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, },
          new BigInteger("1"),
          new BigInteger("1"),
          Integer.parseUnsignedInt("1"),
        },
        new Object[] {
          new byte[] {(byte) 0x7F, },
          new BigInteger("127"),
          new BigInteger("127"),
          Integer.parseUnsignedInt("127"),
        },
        new Object[] {
          new byte[] {(byte) 0x80, },
          new BigInteger("-128"),
          new BigInteger("128"),
          Integer.parseUnsignedInt("128"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, },
          new BigInteger("-1"),
          new BigInteger("255"),
          Integer.parseUnsignedInt("255"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0x00, },
          new BigInteger("256"),
          new BigInteger("256"),
          Integer.parseUnsignedInt("256"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0x7F, },
          new BigInteger("383"),
          new BigInteger("383"),
          Integer.parseUnsignedInt("383"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0x80, },
          new BigInteger("384"),
          new BigInteger("384"),
          Integer.parseUnsignedInt("384"),
        },
        new Object[] {
          new byte[] {(byte) 0x01, (byte) 0xFF, },
          new BigInteger("511"),
          new BigInteger("511"),
          Integer.parseUnsignedInt("511"),
        },
        new Object[] {
          new byte[] {(byte) 0x02, (byte) 0x00, },
          new BigInteger("512"),
          new BigInteger("512"),
          Integer.parseUnsignedInt("512"),
        },
        new Object[] {
          new byte[] {(byte) 0x7F, (byte) 0x00, },
          new BigInteger("32512"),
          new BigInteger("32512"),
          Integer.parseUnsignedInt("32512"),
        },
        new Object[] {
          new byte[] {(byte) 0x7F, (byte) 0xFF, },
          new BigInteger("32767"),
          new BigInteger("32767"),
          Integer.parseUnsignedInt("32767"),
        },
        new Object[] {
          new byte[] {(byte) 0x80, (byte) 0x00, },
          new BigInteger("-32768"),
          new BigInteger("32768"),
          Integer.parseUnsignedInt("32768"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0x00, },
          new BigInteger("-256"),
          new BigInteger("65280"),
          Integer.parseUnsignedInt("65280"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0x7F, },
          new BigInteger("-129"),
          new BigInteger("65407"),
          Integer.parseUnsignedInt("65407"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0xFF, },
          new BigInteger("-1"),
          new BigInteger("65535"),
          Integer.parseUnsignedInt("65535"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, },
          new BigInteger("-1"),
          new BigInteger("16777215"),
          Integer.parseUnsignedInt("16777215"),
        },
        new Object[] {
          new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, },
          new BigInteger("-1"),
          new BigInteger("4294967295"),
          Integer.parseUnsignedInt("4294967295"),
        },
      };
  }


  /**
   * @param  bytes  to decode.
   * @param  expected  int to compare.
   * @param  unsigned  int to compare.
   * @param  unsignedPrim  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "ints")
  public void decode(final byte[] bytes, final BigInteger expected, final BigInteger unsigned, final int unsignedPrim)
    throws Exception
  {
    Assert.assertEquals(IntegerType.decode(new DefaultDERBuffer(bytes)), expected);
  }


  /**
   * @param  bytes  to decode.
   * @param  expected  int to compare.
   * @param  unsigned  int to compare.
   * @param  unsignedPrim  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "ints")
  public void decodeUnsigned(
    final byte[] bytes, final BigInteger expected, final BigInteger unsigned, final int unsignedPrim)
    throws Exception
  {
    Assert.assertEquals(IntegerType.decodeUnsigned(new DefaultDERBuffer(bytes)), unsigned);
  }


  /**
   * @param  bytes  to decode.
   * @param  expected  int to compare.
   * @param  unsigned  int to compare.
   * @param  unsignedPrim  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "ints")
  public void decodeUnsignedPrimitive(
    final byte[] bytes, final BigInteger expected, final BigInteger unsigned, final int unsignedPrim)
    throws Exception
  {
    Assert.assertEquals(IntegerType.decodeUnsignedPrimitive(new DefaultDERBuffer(bytes)), unsignedPrim);
  }


  /**
   * @param  expected  bytes to compare.
   * @param  integer  to encode.
   * @param  unsigned  int to compare.
   * @param  unsignedPrim  int to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "asn1", dataProvider = "ints")
  public void encode(
    final byte[] expected, final BigInteger integer, final BigInteger unsigned, final int unsignedPrim)
    throws Exception
  {
    if (integer.intValue() == -1 && expected.length > 1) {
      // ignore multi byte negative 1
      Assert.assertTrue(expected.length > 1);
    } else {
      Assert.assertEquals(IntegerType.toBytes(integer), expected);
    }
  }
}
