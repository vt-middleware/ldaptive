/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CompareRequest}.
 *
 * @author  Middleware Services
 */
public class CompareRequestTest
{


  /**
   * Request test data.
   *
   * @return  test data
   */
  @DataProvider(name = "requests")
  public Object[][] createRequests()
  {
    return
      new Object[][] {
        new Object[] {
          new CompareRequest(null, null), "attribute=null",
        },
        new Object[] {
          new CompareRequest(null, new LdapAttribute()), "attribute=[null[]]",
        },
        new Object[] {
          new CompareRequest(null, new LdapAttribute("name", "value")), "attribute=[name[value]]",
        },
        new Object[] {
          new CompareRequest(null, new LdapAttribute("userPassword", "password")), "attribute=<suppressed>",
        },
      };
  }


  /**
   * @param  request  containing properties
   * @param  string  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "compare", dataProvider = "requests")
  public void testToString(final CompareRequest request, final String string)
    throws Exception
  {
    Assert.assertEquals(request.toString().split(",")[1].trim(), string);
  }
}
