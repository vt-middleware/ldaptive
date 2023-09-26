/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.Test;

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
    Assert.assertEquals(LdapUtils.trimSpace(""), "");
    Assert.assertEquals(LdapUtils.trimSpace(" "), "");
    Assert.assertEquals(LdapUtils.trimSpace("  "), "");
    Assert.assertEquals(LdapUtils.trimSpace("   "), "");
    Assert.assertEquals(LdapUtils.trimSpace("text"), "text");
    Assert.assertEquals(LdapUtils.trimSpace(" text"), "text");
    Assert.assertEquals(LdapUtils.trimSpace("  text"), "text");
    Assert.assertEquals(LdapUtils.trimSpace("   text"), "text");
    Assert.assertEquals(LdapUtils.trimSpace("text "), "text");
    Assert.assertEquals(LdapUtils.trimSpace("text  "), "text");
    Assert.assertEquals(LdapUtils.trimSpace("text   "), "text");
    Assert.assertEquals(LdapUtils.trimSpace(" text "), "text");
    Assert.assertEquals(LdapUtils.trimSpace("  text  "), "text");
    Assert.assertEquals(LdapUtils.trimSpace("   text   "), "text");
    Assert.assertEquals(LdapUtils.trimSpace("text with spaces"), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace(" text with spaces"), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("  text with spaces"), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("   text with spaces"), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("text with spaces "), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("text with spaces  "), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("text with spaces   "), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace(" text with spaces "), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("  text with spaces  "), "text with spaces");
    Assert.assertEquals(LdapUtils.trimSpace("   text with spaces   "), "text with spaces");
  }


  @Test
  public void testCompressSpaceTrim()
  {
    Assert.assertEquals(LdapUtils.compressSpace("", true), "");
    Assert.assertEquals(LdapUtils.compressSpace(" ", true), "");
    Assert.assertEquals(LdapUtils.compressSpace("  ", true), "");
    Assert.assertEquals(LdapUtils.compressSpace("   ", true), "");

    Assert.assertEquals(LdapUtils.compressSpace("text", true), "text");
    Assert.assertEquals(LdapUtils.compressSpace(" TEXT", true), "TEXT");
    Assert.assertEquals(LdapUtils.compressSpace("  TEXT", true), "TEXT");
    Assert.assertEquals(LdapUtils.compressSpace("   text", true), "text");

    Assert.assertEquals(LdapUtils.compressSpace("text ", true), "text");
    Assert.assertEquals(LdapUtils.compressSpace("TEXT  ", true), "TEXT");
    Assert.assertEquals(LdapUtils.compressSpace("text   ", true), "text");

    Assert.assertEquals(LdapUtils.compressSpace(" text ", true), "text");
    Assert.assertEquals(LdapUtils.compressSpace("  tEXt  ", true), "tEXt");
    Assert.assertEquals(LdapUtils.compressSpace("   text   ", true), "text");

    Assert.assertEquals(LdapUtils.compressSpace("text with spaces", true), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace(" text WITH spaces", true), "text WITH spaces");
    Assert.assertEquals(LdapUtils.compressSpace("  text WITH spaces", true), "text WITH spaces");
    Assert.assertEquals(LdapUtils.compressSpace("   text with spaces", true), "text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace("text with spaces ", true), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("text with SPACES  ", true), "text with SPACES");
    Assert.assertEquals(LdapUtils.compressSpace("text with spaces   ", true), "text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace(" text with spaces ", true), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("  TEXT with spaces  ", true), "TEXT with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("   text with spaces   ", true), "text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace("text  with  spaces", true), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace(" TEXT   with   spaces", true), "TEXT with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("  text  with  SPACES", true), "text with SPACES");
    Assert.assertEquals(LdapUtils.compressSpace("   text   with   spaces", true), "text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace("text  with  spaces ", true), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("text   WITH   spaces  ", true), "text WITH spaces");
    Assert.assertEquals(LdapUtils.compressSpace("text    with    spaces   ", true), "text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace(" text  with  spaces ", true), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("  TEXT   with   SPACES  ", true), "TEXT with SPACES");
    Assert.assertEquals(LdapUtils.compressSpace("   text    with    spaces   ", true), "text with spaces");
  }


  @Test
  public void testCompressSpaceNoTrim()
  {
    Assert.assertEquals(LdapUtils.compressSpace("", false), "");
    Assert.assertEquals(LdapUtils.compressSpace(" ", false), " ");
    Assert.assertEquals(LdapUtils.compressSpace("  ", false), " ");
    Assert.assertEquals(LdapUtils.compressSpace("   ", false), " ");

    Assert.assertEquals(LdapUtils.compressSpace("text", false), "text");
    Assert.assertEquals(LdapUtils.compressSpace(" TEXT", false), " TEXT");
    Assert.assertEquals(LdapUtils.compressSpace("  TEXT", false), " TEXT");
    Assert.assertEquals(LdapUtils.compressSpace("   text", false), " text");

    Assert.assertEquals(LdapUtils.compressSpace("text ", false), "text ");
    Assert.assertEquals(LdapUtils.compressSpace("TEXT  ", false), "TEXT ");
    Assert.assertEquals(LdapUtils.compressSpace("text   ", false), "text ");

    Assert.assertEquals(LdapUtils.compressSpace(" text ", false), " text ");
    Assert.assertEquals(LdapUtils.compressSpace("  tEXt  ", false), " tEXt ");
    Assert.assertEquals(LdapUtils.compressSpace("   text   ", false), " text ");

    Assert.assertEquals(LdapUtils.compressSpace("text with spaces", false), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace(" text WITH spaces", false), " text WITH spaces");
    Assert.assertEquals(LdapUtils.compressSpace("  text WITH spaces", false), " text WITH spaces");
    Assert.assertEquals(LdapUtils.compressSpace("   text with spaces", false), " text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace("text with spaces ", false), "text with spaces ");
    Assert.assertEquals(LdapUtils.compressSpace("text with SPACES  ", false), "text with SPACES ");
    Assert.assertEquals(LdapUtils.compressSpace("text with spaces   ", false), "text with spaces ");

    Assert.assertEquals(LdapUtils.compressSpace(" text with spaces ", false), " text with spaces ");
    Assert.assertEquals(LdapUtils.compressSpace("  TEXT with spaces  ", false), " TEXT with spaces ");
    Assert.assertEquals(LdapUtils.compressSpace("   text with spaces   ", false), " text with spaces ");

    Assert.assertEquals(LdapUtils.compressSpace("text  with  spaces", false), "text with spaces");
    Assert.assertEquals(LdapUtils.compressSpace(" TEXT   with   spaces", false), " TEXT with spaces");
    Assert.assertEquals(LdapUtils.compressSpace("  text  with  SPACES", false), " text with SPACES");
    Assert.assertEquals(LdapUtils.compressSpace("   text   with   spaces", false), " text with spaces");

    Assert.assertEquals(LdapUtils.compressSpace("text  with  spaces ", false), "text with spaces ");
    Assert.assertEquals(LdapUtils.compressSpace("text   WITH   spaces  ", false), "text WITH spaces ");
    Assert.assertEquals(LdapUtils.compressSpace("text    with    spaces   ", false), "text with spaces ");

    Assert.assertEquals(LdapUtils.compressSpace(" text  with  spaces ", false), " text with spaces ");
    Assert.assertEquals(LdapUtils.compressSpace("  TEXT   with   SPACES  ", false), " TEXT with SPACES ");
    Assert.assertEquals(LdapUtils.compressSpace("   text    with    spaces   ", false), " text with spaces ");
  }
}
