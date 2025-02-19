/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.math.BigInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link BigIntegerValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class BigIntegerValueTranscoderTest
{

  /** Transcoder to test. */
  private final BigIntegerValueTranscoder transcoder = new BigIntegerValueTranscoder();


  /**
   * Number test data.
   *
   * @return  numbers
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "numbers")
  public Object[][] createNumbers()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {BigInteger.ZERO, "0", },
        new Object[] {BigInteger.ONE, "1", },
        new Object[] {new BigInteger("-1"), "-1", },
        new Object[] {BigInteger.TWO, "2", },
        new Object[] {new BigInteger("-2"), "-2", },
        new Object[] {BigInteger.TEN, "10", },
        new Object[] {new BigInteger("-10"), "-10", },
        new Object[] {BigInteger.valueOf(Integer.MIN_VALUE), "-2147483648", },
        new Object[] {BigInteger.valueOf(Integer.MAX_VALUE), "2147483647", },
        new Object[] {BigInteger.valueOf(Long.MIN_VALUE), "-9223372036854775808", },
        new Object[] {BigInteger.valueOf(Long.MAX_VALUE), "9223372036854775807", },
      };
  }


  /**
   * Number test data.
   *
   * @return  dates
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "invalid")
  public Object[][] createInvalidNumbers()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {"10.1"},
        new Object[] {"-10.1"},
        new Object[] {"1.2.3.4"},
      };
  }


  /**
   * @param  number  to compare
   * @param  string  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "numbers")
  public void decode(final BigInteger number, final String string)
    throws Exception
  {
    assertThat(transcoder.decodeStringValue(string)).isEqualTo(number);
  }


  /**
   * @param  number  to compare
   * @param  string  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "numbers")
  public void encode(final BigInteger number, final String string)
    throws Exception
  {
    assertThat(transcoder.encodeStringValue(number)).isEqualTo(string);
  }


  /**
   * @param  number  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "invalid")
  public void testInvalid(final String number)
    throws Exception
  {
    try {
      transcoder.decodeStringValue(number);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(NumberFormatException.class);
    }
  }
}
