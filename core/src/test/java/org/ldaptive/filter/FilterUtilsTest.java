/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.ldaptive.LdapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link FilterUtils}.
 *
 * @author  Middleware Services
 */
public class FilterUtilsTest
{


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
          "name",
          "name",
        },
        new Object[] {
          "*name*",
          "\\2Aname\\2A",
        },
        new Object[] {
          "*name",
          "\\2Aname",
        },
        new Object[] {
          "*name*",
          "\\2Aname\\2A",
        },
        new Object[] {
          "(name)",
          "\\28name\\29",
        },
        new Object[] {
          "(name",
          "\\28name",
        },
        new Object[] {
          "name)",
          "name\\29",
        },
        new Object[] {
          "n(am)e",
          "n\\28am\\29e",
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
        new Object[] {
          "Лаборатория",
          "\\D0\\9B\\D0\\B0\\D0\\B1\\D0\\BE\\D1\\80\\D0\\B0\\D1\\82\\D0\\BE\\D1\\80\\D0\\B8\\D1\\8F",
        },
        new Object[] {
          "Университет",
          "\\D0\\A3\\D0\\BD\\D0\\B8\\D0\\B2\\D0\\B5\\D1\\80\\D1\\81\\D0\\B8\\D1\\82\\D0\\B5\\D1\\82",
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
  public void escapeAndParse(final String value, final String match)
    throws Exception
  {
    assertThat(FilterUtils.escape(value)).isEqualTo(match);
    assertThat(FilterUtils.parseAssertionValue(match)).isEqualTo(LdapUtils.utf8Encode(value));
  }
}
