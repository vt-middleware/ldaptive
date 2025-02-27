/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapUtils}.
 *
 * @author  Middleware Services
 */
public class LdapUtilsTest
{

  /** Printable ascii characters. */
  private static final String ASCII_PRINTABLE = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`" +
    "abcdefghijklmnopqrstuvwxyz{|}~";


  @Test
  public void testTrimSpace()
  {
    assertThat(LdapUtils.trimSpace("")).isEqualTo("");
    assertThat(LdapUtils.trimSpace(" ")).isEqualTo("");
    assertThat(LdapUtils.trimSpace("  ")).isEqualTo("");
    assertThat(LdapUtils.trimSpace("   ")).isEqualTo("");
    assertThat(LdapUtils.trimSpace("text")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace(" text")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("  text")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("   text")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("text ")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("text  ")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("text   ")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace(" text ")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("  text  ")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("   text   ")).isEqualTo("text");
    assertThat(LdapUtils.trimSpace("text with spaces")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace(" text with spaces")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("  text with spaces")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("   text with spaces")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("text with spaces ")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("text with spaces  ")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("text with spaces   ")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace(" text with spaces ")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("  text with spaces  ")).isEqualTo("text with spaces");
    assertThat(LdapUtils.trimSpace("   text with spaces   ")).isEqualTo("text with spaces");
  }


  @Test
  public void testCompressSpaceTrim()
  {
    assertThat(LdapUtils.compressSpace("", true)).isEqualTo("");
    assertThat(LdapUtils.compressSpace(" ", true)).isEqualTo("");
    assertThat(LdapUtils.compressSpace("  ", true)).isEqualTo("");
    assertThat(LdapUtils.compressSpace("   ", true)).isEqualTo("");

    assertThat(LdapUtils.compressSpace("text", true)).isEqualTo("text");
    assertThat(LdapUtils.compressSpace(" TEXT", true)).isEqualTo("TEXT");
    assertThat(LdapUtils.compressSpace("  TEXT", true)).isEqualTo("TEXT");
    assertThat(LdapUtils.compressSpace("   text", true)).isEqualTo("text");

    assertThat(LdapUtils.compressSpace("text ", true)).isEqualTo("text");
    assertThat(LdapUtils.compressSpace("TEXT  ", true)).isEqualTo("TEXT");
    assertThat(LdapUtils.compressSpace("text   ", true)).isEqualTo("text");

    assertThat(LdapUtils.compressSpace(" text ", true)).isEqualTo("text");
    assertThat(LdapUtils.compressSpace("  tEXt  ", true)).isEqualTo("tEXt");
    assertThat(LdapUtils.compressSpace("   text   ", true)).isEqualTo("text");

    assertThat(LdapUtils.compressSpace("text with spaces", true)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace(" text WITH spaces", true)).isEqualTo("text WITH spaces");
    assertThat(LdapUtils.compressSpace("  text WITH spaces", true)).isEqualTo("text WITH spaces");
    assertThat(LdapUtils.compressSpace("   text with spaces", true)).isEqualTo("text with spaces");

    assertThat(LdapUtils.compressSpace("text with spaces ", true)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace("text with SPACES  ", true)).isEqualTo("text with SPACES");
    assertThat(LdapUtils.compressSpace("text with spaces   ", true)).isEqualTo("text with spaces");

    assertThat(LdapUtils.compressSpace(" text with spaces ", true)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace("  TEXT with spaces  ", true)).isEqualTo("TEXT with spaces");
    assertThat(LdapUtils.compressSpace("   text with spaces   ", true)).isEqualTo("text with spaces");

    assertThat(LdapUtils.compressSpace("text  with  spaces", true)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace(" TEXT   with   spaces", true)).isEqualTo("TEXT with spaces");
    assertThat(LdapUtils.compressSpace("  text  with  SPACES", true)).isEqualTo("text with SPACES");
    assertThat(LdapUtils.compressSpace("   text   with   spaces", true)).isEqualTo("text with spaces");

    assertThat(LdapUtils.compressSpace("text  with  spaces ", true)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace("text   WITH   spaces  ", true)).isEqualTo("text WITH spaces");
    assertThat(LdapUtils.compressSpace("text    with    spaces   ", true)).isEqualTo("text with spaces");

    assertThat(LdapUtils.compressSpace(" text  with  spaces ", true)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace("  TEXT   with   SPACES  ", true)).isEqualTo("TEXT with SPACES");
    assertThat(LdapUtils.compressSpace("   text    with    spaces   ", true)).isEqualTo("text with spaces");
  }


  @Test
  public void testCompressSpaceNoTrim()
  {
    assertThat(LdapUtils.compressSpace("", false)).isEqualTo("");
    assertThat(LdapUtils.compressSpace(" ", false)).isEqualTo(" ");
    assertThat(LdapUtils.compressSpace("  ", false)).isEqualTo(" ");
    assertThat(LdapUtils.compressSpace("   ", false)).isEqualTo(" ");

    assertThat(LdapUtils.compressSpace("text", false)).isEqualTo("text");
    assertThat(LdapUtils.compressSpace(" TEXT", false)).isEqualTo(" TEXT");
    assertThat(LdapUtils.compressSpace("  TEXT", false)).isEqualTo(" TEXT");
    assertThat(LdapUtils.compressSpace("   text", false)).isEqualTo(" text");

    assertThat(LdapUtils.compressSpace("text ", false)).isEqualTo("text ");
    assertThat(LdapUtils.compressSpace("TEXT  ", false)).isEqualTo("TEXT ");
    assertThat(LdapUtils.compressSpace("text   ", false)).isEqualTo("text ");

    assertThat(LdapUtils.compressSpace(" text ", false)).isEqualTo(" text ");
    assertThat(LdapUtils.compressSpace("  tEXt  ", false)).isEqualTo(" tEXt ");
    assertThat(LdapUtils.compressSpace("   text   ", false)).isEqualTo(" text ");

    assertThat(LdapUtils.compressSpace("text with spaces", false)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace(" text WITH spaces", false)).isEqualTo(" text WITH spaces");
    assertThat(LdapUtils.compressSpace("  text WITH spaces", false)).isEqualTo(" text WITH spaces");
    assertThat(LdapUtils.compressSpace("   text with spaces", false)).isEqualTo(" text with spaces");

    assertThat(LdapUtils.compressSpace("text with spaces ", false)).isEqualTo("text with spaces ");
    assertThat(LdapUtils.compressSpace("text with SPACES  ", false)).isEqualTo("text with SPACES ");
    assertThat(LdapUtils.compressSpace("text with spaces   ", false)).isEqualTo("text with spaces ");

    assertThat(LdapUtils.compressSpace(" text with spaces ", false)).isEqualTo(" text with spaces ");
    assertThat(LdapUtils.compressSpace("  TEXT with spaces  ", false)).isEqualTo(" TEXT with spaces ");
    assertThat(LdapUtils.compressSpace("   text with spaces   ", false)).isEqualTo(" text with spaces ");

    assertThat(LdapUtils.compressSpace("text  with  spaces", false)).isEqualTo("text with spaces");
    assertThat(LdapUtils.compressSpace(" TEXT   with   spaces", false)).isEqualTo(" TEXT with spaces");
    assertThat(LdapUtils.compressSpace("  text  with  SPACES", false)).isEqualTo(" text with SPACES");
    assertThat(LdapUtils.compressSpace("   text   with   spaces", false)).isEqualTo(" text with spaces");

    assertThat(LdapUtils.compressSpace("text  with  spaces ", false)).isEqualTo("text with spaces ");
    assertThat(LdapUtils.compressSpace("text   WITH   spaces  ", false)).isEqualTo("text WITH spaces ");
    assertThat(LdapUtils.compressSpace("text    with    spaces   ", false)).isEqualTo("text with spaces ");

    assertThat(LdapUtils.compressSpace(" text  with  spaces ", false)).isEqualTo(" text with spaces ");
    assertThat(LdapUtils.compressSpace("  TEXT   with   SPACES  ", false)).isEqualTo(" TEXT with SPACES ");
    assertThat(LdapUtils.compressSpace("   text    with    spaces   ", false)).isEqualTo(" text with spaces ");
  }


  /**
   * percentEncode test data.
   *
   * @return  test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "percentEncodeControlChars")
  public Object[][] percentEncodeData()
    throws Exception
  {
    return
      new Object[][]{
        new Object[] {null, null},
        new Object[] {"", ""},
        new Object[] {"No encoding", "No encoding"},
        new Object[] {ASCII_PRINTABLE, ASCII_PRINTABLE},
        new Object[] {"encode: " + (char) 0x00 + " char", "encode: %00 char", },
        new Object[] {"encode: " + (char) 0x01 + " char", "encode: %01 char", },
        new Object[] {"encode: " + (char) 0x02 + " char", "encode: %02 char", },
        new Object[] {"encode: " + (char) 0x03 + " char", "encode: %03 char", },
        new Object[] {"encode: " + (char) 0x04 + " char", "encode: %04 char", },
        new Object[] {"encode: " + (char) 0x05 + " char", "encode: %05 char", },
        new Object[] {"encode: " + (char) 0x06 + " char", "encode: %06 char", },
        new Object[] {"encode: " + (char) 0x07 + " char", "encode: %07 char", },
        new Object[] {"encode: " + (char) 0x08 + " char", "encode: %08 char", },
        new Object[] {"encode: " + (char) 0x09 + " char", "encode: %09 char", },
        new Object[] {"encode: " + (char) 0x0A + " char", "encode: %0A char", },
        new Object[] {"encode: " + (char) 0x0B + " char", "encode: %0B char", },
        new Object[] {"encode: " + (char) 0x0C + " char", "encode: %0C char", },
        new Object[] {"encode: " + (char) 0x0D + " char", "encode: %0D char", },
        new Object[] {"encode: " + (char) 0x0E + " char", "encode: %0E char", },
        new Object[] {"encode: " + (char) 0x0F + " char", "encode: %0F char", },
        new Object[] {"encode: " + (char) 0x10 + " char", "encode: %10 char", },
        new Object[] {"encode: " + (char) 0x11 + " char", "encode: %11 char", },
        new Object[] {"encode: " + (char) 0x12 + " char", "encode: %12 char", },
        new Object[] {"encode: " + (char) 0x13 + " char", "encode: %13 char", },
        new Object[] {"encode: " + (char) 0x14 + " char", "encode: %14 char", },
        new Object[] {"encode: " + (char) 0x15 + " char", "encode: %15 char", },
        new Object[] {"encode: " + (char) 0x16 + " char", "encode: %16 char", },
        new Object[] {"encode: " + (char) 0x17 + " char", "encode: %17 char", },
        new Object[] {"encode: " + (char) 0x18 + " char", "encode: %18 char", },
        new Object[] {"encode: " + (char) 0x19 + " char", "encode: %19 char", },
        new Object[] {"encode: " + (char) 0x1A + " char", "encode: %1A char", },
        new Object[] {"encode: " + (char) 0x1B + " char", "encode: %1B char", },
        new Object[] {"encode: " + (char) 0x1C + " char", "encode: %1C char", },
        new Object[] {"encode: " + (char) 0x1D + " char", "encode: %1D char", },
        new Object[] {"encode: " + (char) 0x1E + " char", "encode: %1E char", },
        new Object[] {"encode: " + (char) 0x1F + " char", "encode: %1F char", },
        new Object[] {"encode: " + (char) 0x7F + " char", "encode: %7F char", },
        // extended ascii
        new Object[] {"encode: € char", "encode: € char", },
        new Object[] {"encode: Ž char", "encode: Ž char", },
        new Object[] {"encode: ÿ char", "encode: ÿ char", },
        // unicode
        new Object[] {"encode: \u20A0 char", "encode: \u20A0 char", },
        // non UTF-8
        new Object[] {"encode: \uDF01 char", "encode: \uDF01 char", },
      };
  }


  @Test(dataProvider = "percentEncodeControlChars")
  public void testPercentEncodeControlChars(final String value, final String encoded)
  {
    assertThat(LdapUtils.percentEncodeControlChars(value))
      .withFailMessage(
        "Percent-encoded value '%s' does not match '%s'", LdapUtils.percentEncodeControlChars(value), encoded)
      .isEqualTo(encoded);
  }


  /**
   * shouldBase64Encode test data.
   *
   * @return  test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "shouldBase64Encode")
  public Object[][] shouldBase64EncodeData()
    throws Exception
  {
    return
      new Object[][] {
        // never encode
        new Object[] {null, false, false, },
        new Object[] {null, true, false, },
        new Object[] {"", false, false, },
        new Object[] {"", true, false, },
        new Object[] {new byte[0], false, false, },
        new Object[] {new byte[0], true, false, },
        new Object[] {ASCII_PRINTABLE, false, false, },
        new Object[] {ASCII_PRINTABLE, true, false, },
        new Object[] {ASCII_PRINTABLE.getBytes(StandardCharsets.UTF_8), false, false, },
        new Object[] {ASCII_PRINTABLE.getBytes(StandardCharsets.UTF_8), true, false, },
        // always encode leading space
        new Object[] {" ", false, true, },
        new Object[] {" ", true, true, },
        new Object[] {new byte[] {' '}, false, true, },
        new Object[] {new byte[] {' '}, false, true, },
        new Object[] {" abc", false, true, },
        new Object[] {" abc", true, true, },
        new Object[] {new byte[] {' ', 'a', 'b', 'c'}, false, true, },
        new Object[] {new byte[] {' ', 'a', 'b', 'c'}, true, true, },
        // always encode trailing space
        new Object[] {"abc ", false, true, },
        new Object[] {"abc ", true, true, },
        new Object[] {new byte[] {'a', 'b', 'c', ' '}, false, true, },
        new Object[] {new byte[] {'a', 'b', 'c', ' '}, true, true, },
        // always encode leading colon
        new Object[] {":", false, true, },
        new Object[] {":", true, true, },
        new Object[] {new byte[] {':'}, false, true, },
        new Object[] {new byte[] {':'}, true, true, },
        new Object[] {":abc", false, true, },
        new Object[] {":abc", true, true, },
        new Object[] {new byte[] {':', 'a', 'b', 'c'}, false, true, },
        new Object[] {new byte[] {':', 'a', 'b', 'c'}, true, true, },
        // always encode leading less than
        new Object[] {"<", false, true, },
        new Object[] {"<", true, true, },
        new Object[] {new byte[] {'<'}, false, true, },
        new Object[] {new byte[] {'<'}, true, true, },
        new Object[] {"<abc", false, true, },
        new Object[] {"<abc", true, true, },
        new Object[] {new byte[] {'<', 'a', 'b', 'c'}, false, true, },
        new Object[] {new byte[] {'<', 'a', 'b', 'c'}, true, true, },
        // always encode NUL
        new Object[] {String.valueOf((char) 0x00), false, true, },
        new Object[] {String.valueOf((char) 0x00), true, true, },
        new Object[] {new byte[] {0x00}, false, true, },
        new Object[] {new byte[] {0x00}, true, true, },
        new Object[] {(char) 0x00 + "xyz", false, true, },
        new Object[] {(char) 0x00 + "xyz", true, true, },
        new Object[] {new byte[] {0x00, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {0x00, 'x', 'y', 'z'}, true, true, },
        new Object[] {"ABC" + (char) 0x00 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x00 + "xyz", true, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x00, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x00, 'x', 'y', 'z'}, true, true, },
        new Object[] {"ABC" + (char) 0x00, false, true, },
        new Object[] {"ABC" + (char) 0x00, true, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x00}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x00}, true, true, },
        // always encode LF
        new Object[] {(char) 0x0A + "xyz", false, true, },
        new Object[] {(char) 0x0A + "xyz", true, true, },
        new Object[] {new byte[] {0x0A, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {0x0A, 'x', 'y', 'z'}, true, true, },
        new Object[] {"ABC" + (char) 0x0A + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x0A + "xyz", true, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0A, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0A, 'x', 'y', 'z'}, true, true, },
        new Object[] {"ABC" + (char) 0x0A, false, true, },
        new Object[] {"ABC" + (char) 0x0A, true, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0A}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0A}, true, true, },
        // always encode CR
        new Object[] {(char) 0x0D + "xyz", false, true, },
        new Object[] {(char) 0x0D + "xyz", true, true, },
        new Object[] {new byte[] {0x0D, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {0x0D, 'x', 'y', 'z'}, true, true, },
        new Object[] {"ABC" + (char) 0x0D + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x0D + "xyz", true, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0D, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0D, 'x', 'y', 'z'}, true, true, },
        new Object[] {"ABC" + (char) 0x0D, false, true, },
        new Object[] {"ABC" + (char) 0x0D, true, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0D}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0D}, true, true, },
        // encode non-printable ascii based on strict flag; 0x01-0x1F and 0x7F, excluding 0x0A and 0x0D
        new Object[] {"ABC" + (char) 0x01 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x01 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x02 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x02 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x03 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x03 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x04 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x04 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x05 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x05 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x06 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x06 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x07 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x07 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x08 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x08 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x09 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x09 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x0B + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x0B + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x0C + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x0C + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x0E + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x0E + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x0F + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x0F + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x10 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x10 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x11 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x11 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x12 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x12 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x13 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x13 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x14 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x14 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x15 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x15 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x16 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x16 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x17 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x17 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x18 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x18 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x19 + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x19 + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x1A + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x1A + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x1B + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x1B + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x1C + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x1C + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x1D + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x1D + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x1E + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x1E + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x1F + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x1F + "xyz", true, false, },
        new Object[] {"ABC" + (char) 0x7F + "xyz", false, true, },
        new Object[] {"ABC" + (char) 0x7F + "xyz", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x01, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x01, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x02, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x02, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x03, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x03, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x04, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x04, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x05, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x05, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x06, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x06, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x07, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x07, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x08, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x08, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x09, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x09, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0B, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0B, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0C, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0C, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0E, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0E, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0F, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x0F, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x10, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x10, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x11, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x11, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x12, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x12, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x13, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x13, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x14, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x14, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x15, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x15, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x16, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x16, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x17, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x17, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x18, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x18, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x19, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x19, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1A, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1A, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1B, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1B, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1C, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1C, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1D, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1D, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1E, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1E, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1F, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x1F, 'x', 'y', 'z'}, true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x7F, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', 0x7F, 'x', 'y', 'z'}, true, false, },
        // extended ascii
        new Object[] {"€xyz", false, true, },
        new Object[] {"€xyz", true, false, },
        new Object[] {new byte[] {(byte) 0x80, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {(byte) 0x80, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABC€xyz", false, true, },
        new Object[] {"ABC€xyz", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x80, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x80, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABC€", false, true, },
        new Object[] {"ABC€", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x80}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x80}, true, false, },
        new Object[] {"Žxyz", false, true, },
        new Object[] {"Žxyz", true, false, },
        new Object[] {new byte[] {(byte) 0x8E, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {(byte) 0x8E, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABCŽxyz", false, true, },
        new Object[] {"ABCŽxyz", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x8E, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x8E, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABCŽ", false, true, },
        new Object[] {"ABCŽ", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x8E}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0x8E}, true, false, },
        new Object[] {"ÿxyz", false, true, },
        new Object[] {"ÿxyz", true, false, },
        new Object[] {new byte[] {(byte) 0xFF, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {(byte) 0xFF, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABCÿxyz", false, true, },
        new Object[] {"ABCÿxyz", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xFF, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xFF, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABCÿ", false, true, },
        new Object[] {"ABCÿ", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xFF}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xFF}, true, false, },
        // unicode
        new Object[] {"\u20A0xyz", false, true, },
        new Object[] {"\u20A0xyz", true, false, },
        new Object[] {new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xA0, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {(byte) 0xE2, (byte) 0x82, (byte) 0xA0, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABC\u20A0xyz", false, true, },
        new Object[] {"ABC\u20A0xyz", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xE2, (byte) 0x82, (byte) 0xA0, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xE2, (byte) 0x82, (byte) 0xA0, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABC\u20A0", false, true, },
        new Object[] {"ABC\u20A0", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xE2, (byte) 0x82, (byte) 0xA0}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xE2, (byte) 0x82, (byte) 0xA0}, true, false, },
        // non UTF-8
        new Object[] {"\uDF01xyz", false, true, },
        new Object[] {"\uDF01xyz", true, false, },
        new Object[] {new byte[] {(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD, 'x', 'y', 'z'}, false, true, },
        new Object[] {new byte[] {(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABC\uDF01xyz", false, true, },
        new Object[] {"ABC\uDF01xyz", true, false, },
        new Object[] {
          new byte[] {'A', 'B', 'C', (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD, 'x', 'y', 'z'}, false, true, },
        new Object[] {
          new byte[] {'A', 'B', 'C', (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD, 'x', 'y', 'z'}, true, false, },
        new Object[] {"ABC\uDF01", false, true, },
        new Object[] {"ABC\uDF01", true, false, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD}, false, true, },
        new Object[] {new byte[] {'A', 'B', 'C', (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFD}, true, false, },
      };
  }


  @Test(dataProvider = "shouldBase64Encode")
  public void testShouldBase64Encode(final Object encode, final boolean strict, final boolean shouldEncode)
  {
    if (encode instanceof String) {
      assertThat(LdapUtils.shouldBase64Encode((String) encode, strict))
        .withFailMessage("Failure for %s with strict=%s and shouldEncode=%s", encode, strict, shouldEncode)
        .isEqualTo(shouldEncode);
    } else {
      assertThat(LdapUtils.shouldBase64Encode((byte[]) encode, strict))
        .withFailMessage(
          "Failure for %s with strict=%s and shouldEncode=%s",
          encode != null ? new String(LdapUtils.hexEncode((byte[]) encode)) : null,
          strict,
          shouldEncode)
        .isEqualTo(shouldEncode);
    }
  }
}
