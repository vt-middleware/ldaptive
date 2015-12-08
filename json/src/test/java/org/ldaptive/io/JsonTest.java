/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.StringReader;
import java.io.StringWriter;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link JsonWriter} and {@link JsonReader}.
 *
 * @author  Middleware Services
 */
public class JsonTest
{


  /**
   * Sample ldap data.
   *
   * @return  executor data
   */
  @DataProvider(name = "search-data")
  public Object[][] createTestData()
  {
    final SearchResult result1 = new SearchResult(SortBehavior.ORDERED);
    final LdapEntry entry11 = new LdapEntry(SortBehavior.ORDERED);
    entry11.setDn("uid=818037,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr12 = new LdapAttribute(SortBehavior.ORDERED);
    attr12.setName("givenName");
    attr12.addStringValue("Daniel", "Dan");
    entry11.addAttribute(
      new LdapAttribute("departmentNumber", "066103"),
      attr12,
      new LdapAttribute("sn", "Fisher"));
    result1.addEntry(entry11);

    final SearchResult result2 = new SearchResult(SortBehavior.ORDERED);
    final LdapEntry entry21 = new LdapEntry(SortBehavior.ORDERED);
    entry21.setDn("uid=1095747,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr212 = new LdapAttribute(SortBehavior.ORDERED);
    attr212.setName("givenName");
    attr212.addStringValue("Robert", "Bob");
    entry21.addAttribute(
      new LdapAttribute("uid", "1095747"),
      attr212,
      new LdapAttribute("sn", "Jones"));
    final LdapEntry entry22 = new LdapEntry(SortBehavior.ORDERED);
    entry22.setDn("uid=1141837,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr222 = new LdapAttribute(SortBehavior.ORDERED);
    attr222.setName("givenName");
    attr222.addStringValue("William", "Bill");
    entry22.addAttribute(
      new LdapAttribute("uid", "1141837"),
      attr222,
      new LdapAttribute("sn", "Smith"));
    final LdapEntry entry23 = new LdapEntry(SortBehavior.ORDERED);
    entry23.setDn("uid=1145718,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr232 = new LdapAttribute(SortBehavior.ORDERED);
    attr232.setName("givenName");
    attr232.addStringValue("Thomas", "Tom");
    entry23.addAttribute(
      new LdapAttribute("uid", "1145718"),
      attr232,
      new LdapAttribute("sn", "Johnson"));
    final LdapEntry entry24 = new LdapEntry(SortBehavior.ORDERED);
    entry24.setDn("uid=1152120,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr242 = new LdapAttribute(SortBehavior.ORDERED);
    attr242.setName("givenName");
    attr242.addStringValue("David", "Dave");
    entry24.addAttribute(
      new LdapAttribute("uid", "1152120"),
      attr242,
      new LdapAttribute("sn", "Brown"));
    final LdapEntry entry25 = new LdapEntry(SortBehavior.ORDERED);
    entry25.setDn("uid=818037,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr252 = new LdapAttribute(SortBehavior.ORDERED);
    attr252.setName("givenName");
    attr252.addStringValue("Joseph", "Joe");
    entry25.addAttribute(
      new LdapAttribute("uid", "818037"),
      attr252,
      new LdapAttribute("sn", "Anderson"));
    result2.addEntry(entry21);
    result2.addEntry(entry22);
    result2.addEntry(entry23);
    result2.addEntry(entry24);
    result2.addEntry(entry25);

    return
      new Object[][] {
        // note that search result isn't ordered, so the json isn't either
        {
          result1,
          "[{\"dn\":\"uid=818037,ou=people,dc=ldaptive,dc=org\"," +
              "\"departmentNumber\":[\"066103\"]," +
              "\"givenName\":[\"Daniel\",\"Dan\"]," +
              "\"sn\":[\"Fisher\"]}]",
        },
        {
          result2,
          // CheckStyle:Indentation OFF
          "[{\"dn\":\"uid=1095747,ou=people,dc=ldaptive,dc=org\"," +
              "\"uid\":[\"1095747\"]," +
              "\"givenName\":[\"Robert\",\"Bob\"]," +
              "\"sn\":[\"Jones\"]}," +
           "{\"dn\":\"uid=1141837,ou=people,dc=ldaptive,dc=org\"," +
              "\"uid\":[\"1141837\"]," +
              "\"givenName\":[\"William\",\"Bill\"]," +
              "\"sn\":[\"Smith\"]}," +
           "{\"dn\":\"uid=1145718,ou=people,dc=ldaptive,dc=org\"," +
              "\"uid\":[\"1145718\"]," +
              "\"givenName\":[\"Thomas\",\"Tom\"]," +
              "\"sn\":[\"Johnson\"]}," +
           "{\"dn\":\"uid=1152120,ou=people,dc=ldaptive,dc=org\"," +
              "\"uid\":[\"1152120\"]," +
              "\"givenName\":[\"David\",\"Dave\"]," +
              "\"sn\":[\"Brown\"]}," +
           "{\"dn\":\"uid=818037,ou=people,dc=ldaptive,dc=org\"," +
              "\"uid\":[\"818037\"]," +
              "\"givenName\":[\"Joseph\",\"Joe\"]," +
              "\"sn\":[\"Anderson\"]}]",
          // CheckStyle:Indentation ON
        },
      };
  }


  /**
   * @param  result  to convert to json.
   * @param  json  to expect from the writer.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"jsontest"}, dataProvider = "search-data")
  public void writer(final SearchResult result, final String json)
    throws Exception
  {
    final StringWriter writer = new StringWriter();
    (new JsonWriter(writer)).write(result);
    AssertJUnit.assertEquals(json, writer.toString());
  }


  /**
   * @param  result  to convert to json.
   * @param  json  to expect from the writer.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"jsontest"}, dataProvider = "search-data")
  public void reader(final SearchResult result, final String json)
    throws Exception
  {
    final StringReader reader = new StringReader(json);
    AssertJUnit.assertEquals(result, (new JsonReader(reader)).read());
  }
}
