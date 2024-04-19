/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DefaultAttributeValueEscaper}.
 *
 * @author  Middleware Services
 */
public class DefaultAttributeValueEscaperTest
{

  /** Escaper to test. */
  private final DefaultAttributeValueEscaper escaper = new DefaultAttributeValueEscaper();


  /**
   * Value test data.
   *
   * @return  string values
   */
  @DataProvider(name = "values")
  public Object[][] createValues()
  {
    return
      new Object[][] {
        new Object[] {
          "<brackets>",
          "\\<brackets\\>",
        },
        new Object[] {
          "#pound",
          "\\#pound",
        },
        new Object[] {
          "dog#",
          "dog\\#",
        },
        new Object[] {
          "this==that",
          "this\\=\\=that",
        },
        new Object[] {
          "1+1=5",
          "1\\+1\\=5",
        },
        new Object[] {
          "back\\slash",
          "back\\\\slash",
        },
        new Object[] {
          "The quick brown; fox jumps over, the lazy dog",
          "The quick brown\\; fox jumps over\\, the lazy dog",
        },
        new Object[] {
          " white space ",
          "\\ white space\\ ",
        },
        new Object[] {
          "Theodore \"Ted\" Logan, III",
          "Theodore \\\"Ted\\\" Logan\\, III",
        },
        new Object[] {
          "Luččić",
          "Lu\\C4\\8D\\C4\\8Di\\C4\\87",
        },
        new Object[] {
          "Luččićo",
          "Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
        },
        new Object[] {
          "Runic Letter PERTHO PEORTH \u16C8",
          "Runic Letter PERTHO PEORTH \\E1\\9B\\88",
        },
        new Object[] {
          "Cuneiform Sign UR4 \uD808\uDF34",
          "Cuneiform Sign UR4 \\F0\\92\\8C\\B4",
        },
        new Object[] {
          "Iceland Flag \uD83C\uDDEE\uD83C\uDDF8",
          "Iceland Flag \\F0\\9F\\87\\AE\\F0\\9F\\87\\B8",
        },
        new Object[] {
          "Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F",
          "Pirate Flag \\F0\\9F\\8F\\B4\\E2\\80\\8D\\E2\\98\\A0\\EF\\B8\\8F",
        },
      };
  }


  /**
   * @param  value  to escape.
   * @param  match  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "values")
  public void escape(final String value, final String match)
    throws Exception
  {
    Assert.assertEquals(escaper.escape(value), match);
  }
}
