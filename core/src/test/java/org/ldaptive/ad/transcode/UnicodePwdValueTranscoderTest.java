/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.transcode;

import org.ldaptive.LdapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link UnicodePwdValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class UnicodePwdValueTranscoderTest
{

  /** Transcoder to test. */
  private final UnicodePwdValueTranscoder transcoder = new UnicodePwdValueTranscoder();


  /**
   * Password test data.
   *
   * @return  passwords
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "passwords")
  public Object[][] createPasswords()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {
          "password",
          LdapUtils.base64Decode("IgBwAGEAcwBzAHcAbwByAGQAIgA="),
        },
        new Object[] {
          "The quick brown fox jumps over the lazy dog",
          LdapUtils.base64Decode(
            "IgBUAGgAZQAgAHEAdQBpAGMAawAgAGIAcgBvAHcAbgAgAGYAbwB4ACAAagB1AG0A" +
            "cABzACAAbwB2AGUAcgAgAHQAaABlACAAbABhAHoAeQAgAGQAbwBnACIA"),
        },
        new Object[] {
          "1234567890",
          LdapUtils.base64Decode("IgAxADIAMwA0ADUANgA3ADgAOQAwACIA"),
        },
        new Object[] {
          "`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?",
          LdapUtils.base64Decode(
            "IgBgAH4AIQBAACMAJAAlAF4AJgAqACgAKQAtAF8APQArAFsAewBdAH0AXAB8ADsA" +
            "OgAnACIALAA8AC4APgAvAD8AIgA="),
        },
        new Object[] {
          "",
          LdapUtils.base64Decode("IgAiAA=="),
        },
      };
  }


  /**
   * Password test data.
   *
   * @return  passwords
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "invalid")
  public Object[][] createInvalidPasswords()
    throws Exception
  {
    return new Object[][] {
      new Object[] {
        null,
        new byte[0],
      },
    };
  }


  /**
   * @param  pwd  to encode
   * @param  unicodePwd  encoded password
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "passwords")
  public void testTranscode(final String pwd, final byte[] unicodePwd)
    throws Exception
  {
    assertThat(transcoder.encodeBinaryValue(pwd)).isEqualTo(unicodePwd);
    assertThat(transcoder.decodeBinaryValue(unicodePwd)).isEqualTo(pwd);
    assertThat(transcoder.decodeStringValue(transcoder.encodeStringValue(pwd))).isEqualTo(pwd);
    assertThat(transcoder.encodeStringValue(pwd)).isEqualTo(LdapUtils.utf8Encode(unicodePwd));
    assertThat(transcoder.decodeStringValue(LdapUtils.utf8Encode(unicodePwd))).isEqualTo(pwd);
  }


  /**
   * @param  pwd  to encode
   * @param  unicodePwd  encoded password
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "invalid")
  public void testInvalid(final String pwd, final byte[] unicodePwd)
    throws Exception
  {
    try {
      transcoder.encodeStringValue(pwd);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      transcoder.encodeBinaryValue(pwd);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      transcoder.decodeStringValue(LdapUtils.utf8Encode(unicodePwd));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      transcoder.decodeBinaryValue(unicodePwd);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }
}
