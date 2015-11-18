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
    final LdapEntry entry1_1 = new LdapEntry(SortBehavior.ORDERED);
    entry1_1.setDn("uid=818037,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr1_2 = new LdapAttribute(SortBehavior.ORDERED);
    attr1_2.setName("givenName");
    attr1_2.addStringValue("Daniel", "Dan");
    entry1_1.addAttribute(
      new LdapAttribute("departmentNumber", "066103"),
      attr1_2,
      new LdapAttribute("sn", "Fisher"));
    result1.addEntry(entry1_1);

    final SearchResult result2 = new SearchResult(SortBehavior.ORDERED);
    final LdapEntry entry2_1 = new LdapEntry(SortBehavior.ORDERED);
    entry2_1.setDn("uid=1095747,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr2_1_2 = new LdapAttribute(SortBehavior.ORDERED);
    attr2_1_2.setName("givenName");
    attr2_1_2.addStringValue("Robert", "Bob");
    entry2_1.addAttribute(
      new LdapAttribute("uid", "1095747"),
      attr2_1_2,
      new LdapAttribute("sn", "Jones"));
    final LdapEntry entry2_2 = new LdapEntry(SortBehavior.ORDERED);
    entry2_2.setDn("uid=1141837,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr2_2_2 = new LdapAttribute(SortBehavior.ORDERED);
    attr2_2_2.setName("givenName");
    attr2_2_2.addStringValue("William", "Bill");
    entry2_2.addAttribute(
      new LdapAttribute("uid", "1141837"),
      attr2_2_2,
      new LdapAttribute("sn", "Smith"));
    final LdapEntry entry2_3 = new LdapEntry(SortBehavior.ORDERED);
    entry2_3.setDn("uid=1145718,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr2_3_2 = new LdapAttribute(SortBehavior.ORDERED);
    attr2_3_2.setName("givenName");
    attr2_3_2.addStringValue("Thomas", "Tom");
    entry2_3.addAttribute(
      new LdapAttribute("uid", "1145718"),
      attr2_3_2,
      new LdapAttribute("sn", "Johnson"));
    final LdapEntry entry2_4 = new LdapEntry(SortBehavior.ORDERED);
    entry2_4.setDn("uid=1152120,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr2_4_2 = new LdapAttribute(SortBehavior.ORDERED);
    attr2_4_2.setName("givenName");
    attr2_4_2.addStringValue("David", "Dave");
    entry2_4.addAttribute(
      new LdapAttribute("uid", "1152120"),
      attr2_4_2,
      new LdapAttribute("sn", "Brown"));
    final LdapEntry entry2_5 = new LdapEntry(SortBehavior.ORDERED);
    entry2_5.setDn("uid=818037,ou=people,dc=ldaptive,dc=org");
    final LdapAttribute attr2_5_2 = new LdapAttribute(SortBehavior.ORDERED);
    attr2_5_2.setName("givenName");
    attr2_5_2.addStringValue("Joseph", "Joe");
    entry2_5.addAttribute(
      new LdapAttribute("uid", "818037"),
      attr2_5_2,
      new LdapAttribute("sn", "Anderson"));
    result2.addEntry(entry2_1);
    result2.addEntry(entry2_2);
    result2.addEntry(entry2_3);
    result2.addEntry(entry2_4);
    result2.addEntry(entry2_5);

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
  public void search(final SearchResult result, final String json)
    throws Exception
  {
    final StringWriter writer = new StringWriter();
    (new JsonWriter(writer)).write(result);
    AssertJUnit.assertEquals(json, writer.toString());

    final StringReader reader = new StringReader(json);
    AssertJUnit.assertEquals(result, (new JsonReader(reader)).read());
  }
}
