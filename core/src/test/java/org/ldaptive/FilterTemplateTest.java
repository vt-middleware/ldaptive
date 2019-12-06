/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link FilterTemplate}.
 *
 * @author  Middleware Services
 */
public class FilterTemplateTest
{


  /**
   * Filter template test data.
   *
   * @return  search filters
   */
  @DataProvider(name = "filters")
  public Object[][] createFilters()
  {
    return
      new Object[][] {
        new Object[] {
          "(&(givenName=Bill)(sn=Wallace))",
          FilterTemplate.builder()
            .filter("(&(givenName=Bill)(sn=Wallace))")
            .build(),
        },
        new Object[] {
          "(&(givenName=Bill)(sn=Wallace))",
          FilterTemplate.builder()
            .filter("(&(givenName={0})(sn={1}))")
            .parameter(0, "Bill")
            .parameter(1, "Wallace")
            .build(),
        },
        new Object[] {
          "(&(givenName=Bill)(sn=Wallace))",
          FilterTemplate.builder()
            .filter("(&(givenName={name})(sn={1}))")
            .parameter("name", "Bill")
            .parameter(1, "Wallace")
            .build(),
        },
        new Object[] {
          "(&(givenName=Bill\\2A)(sn=Wa\\28ll\\29ace))",
          FilterTemplate.builder()
            .filter("(&(givenName={firstname})(sn={lastname}))")
            .parameter("firstname", "Bill*")
            .parameter("lastname", "Wa(ll)ace")
            .build(),
        },
        new Object[] {
          "(&(givenName=\\42\\69\\6C\\6C)(sn=\\57\\61\\6C\\6C\\61\\63\\65))",
          FilterTemplate.builder()
            .filter("(&(givenName={firstname})(sn={lastname}))")
            .parameter("firstname", new byte[] {'B', 'i', 'l', 'l', })
            .parameter("lastname", new byte[] {'W', 'a', 'l', 'l', 'a', 'c', 'e', })
            .build(),
        },
        new Object[] {
          "(&(givenName=B\\C3\\ACll))",
          FilterTemplate.builder()
            .filter("(&(givenName={firstname}))")
            .parameter("firstname", "B\u00ECll")
            .build(),
        },
        new Object[] {
          "(&(givenName=B\\E2\\88\\9Ell))",
          FilterTemplate.builder()
            .filter("(&(givenName={firstname}))")
            .parameter("firstname", "B\u221Ell")
            .build(),
        },
        new Object[] {
          "(&(givenName=B\\F0\\9F\\9C\\81ll))",
          FilterTemplate.builder()
            .filter("(&(givenName={firstname}))")
            .parameter("firstname", "B\uD83D\uDF01ll")
            .build(),
        },
      };
  }


  /**
   * @param  encodedFilter  to compare against
   * @param  filter  to format
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "filter", dataProvider = "filters")
  public void testFormat(final String encodedFilter, final FilterTemplate filter)
    throws Exception
  {
    Assert.assertEquals(filter.format(), encodedFilter);
  }
}
