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
    final String f1 = "(&(givenName=Bill)(sn=Wallace))";
    final FilterTemplate sf1 = new FilterTemplate(f1);
    final FilterTemplate sf2 = new FilterTemplate("(&(givenName={0})(sn={1}))");
    sf2.setParameter(0, "Bill");
    sf2.setParameter(1, "Wallace");

    final FilterTemplate sf3 = new FilterTemplate("(&(givenName={name})(sn={1}))");
    sf3.setParameter("name", "Bill");
    sf3.setParameter(1, "Wallace");

    final String f4 = "(&(givenName=Bill\\2A)(sn=Wa\\28ll\\29ace))";
    final FilterTemplate sf4 = new FilterTemplate("(&(givenName={firstname})(sn={lastname}))");
    sf4.setParameter("firstname", "Bill*");
    sf4.setParameter("lastname", "Wa(ll)ace");

    final String f5 = "(&(givenName=\\42\\69\\6C\\6C)(sn=\\57\\61\\6C\\6C\\61\\63\\65))";
    final FilterTemplate sf5 = new FilterTemplate("(&(givenName={firstname})(sn={lastname}))");
    sf5.setParameter("firstname", new byte[] {'B', 'i', 'l', 'l', });
    sf5.setParameter("lastname", new byte[] {'W', 'a', 'l', 'l', 'a', 'c', 'e', });

    final String f6 = "(&(givenName=B\\C3\\ACll))";
    final FilterTemplate sf6 = new FilterTemplate("(&(givenName={firstname}))");
    sf6.setParameter("firstname", "B\u00ECll");

    final String f7 = "(&(givenName=B\\E2\\88\\9Ell))";
    final FilterTemplate sf7 = new FilterTemplate("(&(givenName={firstname}))");
    sf7.setParameter("firstname", "B\u221Ell");

    final String f8 = "(&(givenName=B\\F0\\9F\\9C\\81ll))";
    final FilterTemplate sf8 = new FilterTemplate("(&(givenName={firstname}))");
    sf8.setParameter("firstname", "B\uD83D\uDF01ll");

    return
      new Object[][] {
        new Object[] {f1, sf1, },
        new Object[] {f1, sf2, },
        new Object[] {f1, sf3, },
        new Object[] {f4, sf4, },
        new Object[] {f5, sf5, },
        new Object[] {f6, sf6, },
        new Object[] {f7, sf7, },
        new Object[] {f8, sf8, },
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
