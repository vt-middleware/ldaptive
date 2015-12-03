/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Hex}.
 *
 * @author  Middleware Services
 */
public class HexTest
{


  /**
   * Hex test data.
   *
   * @return  hex test data
   */
  @DataProvider(name = "encode-decode")
  public Object[][] createEncodeDecodeData()
  {
    return
      new Object[][] {
        new Object[] {
          "".getBytes(StandardCharsets.UTF_8),
          "",
        },
        new Object[] {
          "Hello World".getBytes(StandardCharsets.UTF_8),
          "48656C6C6F20576F726C64",
        },
        new Object[] {
          "Hexadecimal Encode".getBytes(StandardCharsets.UTF_8),
          "48657861646563696D616C20456E636F6465",
        },
        new Object[] {
          new Scanner(
            HexTest.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter("\\Z").next().getBytes(StandardCharsets.UTF_8),
          new Scanner(HexTest.class.getResourceAsStream("/org/ldaptive/io/hex.txt")).useDelimiter("\\Z").next(),
        },
      };
  }


  /**
   * Hex test data.
   *
   * @return  hex test data
   */
  @DataProvider(name = "invalid-decode")
  public Object[][] createInvalidDecode()
  {
    return
      new Object[][] {
        // odd characters
        new Object[] {
          new char[] {'A', },
        },
        new Object[] {
          new char[] {'A', 'B', 'C', },
        },
        new Object[] {
          new char[] {'A', 'B', 'C', 'D', 'E', },
        },
        new Object[] {
          new char[] {97},
        },
        // invalid characters
        new Object[] {
          new char[] {'A', 'l', },
        },
        new Object[] {
          new char[] {'l', 'A', },
        },
        new Object[] {
          new char[] {'0', '4', '*', 'b', },
        },
        new Object[] {
          new char[] {'0', '4', 'b', '*', },
        },
      };
  }


  /**
   * @param  raw  data to encode
   * @param  encoded  valid encoding
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"io"}, dataProvider = "encode-decode")
  public void encodeAndDecode(final byte[] raw, final String encoded)
    throws Exception
  {
    final char[] c = Hex.encode(raw);
    Assert.assertEquals(encoded.toCharArray(), c);
    Assert.assertEquals(raw, Hex.decode(c));
  }


  /**
   * @param  data  to decode
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"io"}, dataProvider = "invalid-decode")
  public void decodeException(final char[] data)
    throws Exception
  {
    try {
      Hex.decode(data);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
