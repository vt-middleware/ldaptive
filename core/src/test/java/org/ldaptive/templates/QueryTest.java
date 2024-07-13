/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link Query}.
 *
 * @author  Middleware Services
 */
public class QueryTest
{


  /**
   * Sample query data.
   *
   * @return  query data
   */
  @DataProvider(name = "query-data")
  public Object[][] createTestData()
  {
    return
      new Object[][] {
        {
          new Query(null),
          new String[] {},
        },
        {
          new Query(""),
          new String[] {},
        },
        {
          new Query("  dfisher "),
          new String[] {"dfisher"},
        },
        {
          new Query("d fisher"),
          new String[] {"d", "fisher", },
        },
        {
          new Query("daniel fisher"),
          new String[] {"daniel", "fisher", },
        },
        {
          new Query("daniel w fisher"),
          new String[] {"daniel", "w", "fisher", },
        },
      };
  }


  /**
   * @param  query  to get terms from
   * @param  terms  to compare
   */
  @Test(groups = "querytest", dataProvider = "query-data")
  public void format(final Query query, final String[] terms)
  {
    assertThat(query.getTerms()).isEqualTo(terms);
  }
}
