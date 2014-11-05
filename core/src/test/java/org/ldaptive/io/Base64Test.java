/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.nio.charset.Charset;
import java.util.Scanner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Base64}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class Base64Test
{

  /** UTF-8 character set. */
  private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");


  /**
   * Base64 test data.
   *
   * @return  base64 test data
   */
  @DataProvider(name = "encode-decode")
  public Object[][] createEncodeDecodeData()
  {
    return
      new Object[][] {
        new Object[] {
          "".getBytes(UTF8_CHARSET),
          "",
        },
        new Object[] {
          "Hello World".getBytes(UTF8_CHARSET),
          "SGVsbG8gV29ybGQ=",
        },
        new Object[] {
          "Base64 Encode".getBytes(UTF8_CHARSET),
          "QmFzZTY0IEVuY29kZQ==",
        },
        new Object[] {
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter(
                "\\Z").next().getBytes(UTF8_CHARSET),
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/base64-0.txt")).useDelimiter("\\Z").next(),
        },
      };
  }


  /**
   * Base64 test data.
   *
   * @return  base64 test data
   */
  @DataProvider(name = "decode")
  public Object[][] createDecodeData()
  {
    return
      new Object[][] {
        new Object[] {
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter(
                "\\Z").next().getBytes(UTF8_CHARSET),
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/base64-76.txt")).useDelimiter("\\Z").next(),
        },
        new Object[] {
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter(
                "\\Z").next().getBytes(UTF8_CHARSET),
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/base64-64.txt")).useDelimiter("\\Z").next(),
        },
      };
  }


  /**
   * Base64 test data.
   *
   * @return  base64 test data
   */
  @DataProvider(name = "invalid-decode")
  public Object[][] createInvalidDecode()
  {
    return
      new Object[][] {
        new Object[] {"QmFzZTY0IEVuY29kZQ=", },
        new Object[] {"QmFzZTY0IEVuY29kZQ", },
      };
  }


  /**
   * @param  raw  data to encode
   * @param  encoded  valid encoding
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"io"},
    dataProvider = "encode-decode"
  )
  public void encodeAndDecode(final byte[] raw, final String encoded)
    throws Exception
  {
    final String s = new String(Base64.encodeToByte(raw, false), UTF8_CHARSET);
    Assert.assertEquals(encoded, s);
    Assert.assertEquals(raw, Base64.decode(s));
  }


  /**
   * @param  raw  data to encode
   * @param  encoded  valid encoding
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"io"},
    dataProvider = "decode"
  )
  public void decode(final byte[] raw, final String encoded)
    throws Exception
  {
    Assert.assertEquals(raw, Base64.decode(encoded));
  }


  /**
   * @param  data  to decode
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"io"},
    dataProvider = "invalid-decode"
  )
  public void decodeException(final String data)
    throws Exception
  {
    try {
      Base64.decode(data);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
