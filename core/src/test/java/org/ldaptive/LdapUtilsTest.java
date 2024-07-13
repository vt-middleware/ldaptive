/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapUtils}.
 *
 * @author  Middleware Services
 */
public class LdapUtilsTest
{


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
}
