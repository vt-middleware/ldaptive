/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link Base64}.
 *
 * @author  Middleware Services
 */
public class Base64Test
{


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
          "".getBytes(StandardCharsets.UTF_8),
          "",
        },
        new Object[] {
          "Hello World".getBytes(StandardCharsets.UTF_8),
          "SGVsbG8gV29ybGQ=",
        },
        new Object[] {
          "Base64 Encode".getBytes(StandardCharsets.UTF_8),
          "QmFzZTY0IEVuY29kZQ==",
        },
        new Object[] {
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter("\\Z").next().getBytes(StandardCharsets.UTF_8),
          new Scanner(
            Base64Test.class.getResourceAsStream("/org/ldaptive/io/base64-0.txt")).useDelimiter("\\Z").next(),
        },
      };
  }


  /**
   * Base64 test data.
   *
   * @return  base64 test data
   */
  @DataProvider(name = "decode-mime")
  public Object[][] createDecodeMimeData()
  {
    return
      new Object[][] {
        new Object[] {
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter("\\Z").next().getBytes(StandardCharsets.UTF_8),
          new Scanner(
            Base64Test.class.getResourceAsStream("/org/ldaptive/io/base64-76.txt")).useDelimiter("\\Z").next(),
        },
        new Object[] {
          new Scanner(
            Base64Test.class.getResourceAsStream(
              "/org/ldaptive/io/plaintext.txt")).useDelimiter("\\Z").next().getBytes(StandardCharsets.UTF_8),
          new Scanner(
            Base64Test.class.getResourceAsStream("/org/ldaptive/io/base64-64.txt")).useDelimiter("\\Z").next(),
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
    return new Object[][] {
      new Object[] {"QmFzZTY0IEVuY29kZQå", },
      new Object[] {"QmFzZTY0IEVuY29kZQç", },
    };
  }


  /**
   * @param  raw  data to encode
   * @param  encoded  valid encoding
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "io", dataProvider = "encode-decode")
  public void encodeAndDecode(final byte[] raw, final String encoded)
    throws Exception
  {
    final String s = new String(Base64.getEncoder().encode(raw), StandardCharsets.UTF_8);
    Assert.assertEquals(encoded, s);
    Assert.assertEquals(raw, Base64.getDecoder().decode(s));
  }


  /**
   * @param  raw  data to encode
   * @param  encoded  valid encoding
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "io", dataProvider = "decode-mime")
  public void decodeMime(final byte[] raw, final String encoded)
    throws Exception
  {
    Assert.assertEquals(raw, Base64.getMimeDecoder().decode(encoded));
  }


  /**
   * @param  data  to decode
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "io", dataProvider = "invalid-decode")
  public void decodeException(final String data)
    throws Exception
  {
    try {
      Base64.getDecoder().decode(data);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(IllegalArgumentException.class, e.getClass());
    }
  }
}
