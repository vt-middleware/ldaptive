/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchFilter}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SearchFilterTest
{


  /**
   * Search filter test data.
   *
   * @return  search filters
   */
  @DataProvider(name = "filters")
  public Object[][] createFilters()
  {
    final String f1 = "(&(givenName=Bill)(sn=Wallace))";
    final SearchFilter sf1 = new SearchFilter(f1);
    final SearchFilter sf2 = new SearchFilter("(&(givenName={0})(sn={1}))");
    sf2.setParameter(0, "Bill");
    sf2.setParameter(1, "Wallace");

    final SearchFilter sf3 = new SearchFilter("(&(givenName={name})(sn={1}))");
    sf3.setParameter("name", "Bill");
    sf3.setParameter(1, "Wallace");

    final String f4 = "(&(givenName=Bill\\2a)(sn=Wa\\28ll\\29ace))";
    final SearchFilter sf4 = new SearchFilter(
      "(&(givenName={firstname})(sn={lastname}))");
    sf4.setParameter("firstname", "Bill*");
    sf4.setParameter("lastname", "Wa(ll)ace");

    final String f5 =
      "(&(givenName=\\42\\69\\6C\\6C)(sn=\\57\\61\\6C\\6C\\61\\63\\65))";
    final SearchFilter sf5 = new SearchFilter(
      "(&(givenName={firstname})(sn={lastname}))");
    sf5.setParameter("firstname", new byte[] {'B', 'i', 'l', 'l', });
    sf5.setParameter(
      "lastname",
      new byte[] {'W', 'a', 'l', 'l', 'a', 'c', 'e', });

    return
      new Object[][] {
        new Object[] {f1, sf1, },
        new Object[] {f1, sf2, },
        new Object[] {f1, sf3, },
        new Object[] {f4, sf4, },
        new Object[] {f5, sf5, },
      };
  }


  /**
   * @param  encodedFilter  to compare against
   * @param  filter  to format
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"filter"},
    dataProvider = "filters"
  )
  public void testFormat(final String encodedFilter, final SearchFilter filter)
    throws Exception
  {
    Assert.assertEquals(filter.format(), encodedFilter);
  }
}
