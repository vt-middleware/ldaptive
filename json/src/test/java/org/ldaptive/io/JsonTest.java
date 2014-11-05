/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.StringReader;
import java.io.StringWriter;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
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
    return
      new Object[][] {
        // note that search result isn't ordered, so the json isn't either
        {
          new SearchResult(
            new LdapEntry(
              "uid=818037,ou=people,dc=ldaptive,dc=org",
              new LdapAttribute("departmentNumber", "066103"),
              new LdapAttribute("givenName", "Daniel", "Dan"),
              new LdapAttribute("sn", "Fisher"))),
          "[{\"dn\":\"uid=818037,ou=people,dc=ldaptive,dc=org\"," +
            "\"sn\":[\"Fisher\"],\"givenName\":[\"Dan\",\"Daniel\"]," +
            "\"departmentNumber\":[\"066103\"]}]",
        },
        {
          new SearchResult(
            new LdapEntry(
              "uid=1095747,ou=people,dc=ldaptive,dc=org",
              new LdapAttribute("uid", "1095747"),
              new LdapAttribute("givenName", "Robert", "Bob"),
              new LdapAttribute("sn", "Jones")),
            new LdapEntry(
              "uid=1141837,ou=people,dc=ldaptive,dc=org",
              new LdapAttribute("uid", "1141837"),
              new LdapAttribute("givenName", "William", "Bill"),
              new LdapAttribute("sn", "Smith")),
            new LdapEntry(
              "uid=1145718,ou=people,dc=ldaptive,dc=org",
              new LdapAttribute("uid", "1145718"),
              new LdapAttribute("givenName", "Thomas", "Tom"),
              new LdapAttribute("sn", "Johnson")),
            new LdapEntry(
              "uid=1152120,ou=people,dc=ldaptive,dc=org",
              new LdapAttribute("uid", "1152120"),
              new LdapAttribute("givenName", "David", "Dave"),
              new LdapAttribute("sn", "Brown")),
            new LdapEntry(
              "uid=818037,ou=people,dc=ldaptive,dc=org",
              new LdapAttribute("uid", "818037"),
              new LdapAttribute("givenName", "Joseph", "Joe"),
              new LdapAttribute("sn", "Anderson"))),
          "[{\"dn\":\"uid=1095747,ou=people,dc=ldaptive,dc=org\"," +
            "\"uid\":[\"1095747\"],\"sn\":[\"Jones\"]," +
            "\"givenName\":[\"Robert\",\"Bob\"]}," +
            "{\"dn\":\"uid=818037,ou=people,dc=ldaptive,dc=org\"," +
            "\"uid\":[\"818037\"],\"sn\":[\"Anderson\"]," +
            "\"givenName\":[\"Joseph\",\"Joe\"]}," +
            "{\"dn\":\"uid=1152120,ou=people,dc=ldaptive,dc=org\"," +
            "\"uid\":[\"1152120\"],\"sn\":[\"Brown\"]," +
            "\"givenName\":[\"David\",\"Dave\"]}," +
            "{\"dn\":\"uid=1145718,ou=people,dc=ldaptive,dc=org\"," +
            "\"uid\":[\"1145718\"],\"sn\":[\"Johnson\"]," +
            "\"givenName\":[\"Thomas\",\"Tom\"]}," +
            "{\"dn\":\"uid=1141837,ou=people,dc=ldaptive,dc=org\"," +
            "\"uid\":[\"1141837\"],\"sn\":[\"Smith\"]," +
            "\"givenName\":[\"Bill\",\"William\"]}]",
        },
      };
  }


  /**
   * @param  result  to convert to json.
   * @param  json  to expect from the writer.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"jsontest"},
    dataProvider = "search-data"
  )
  public void search(
    final SearchResult result,
    final String json)
    throws Exception
  {
    final StringWriter writer = new StringWriter();
    (new JsonWriter(writer)).write(result);
    AssertJUnit.assertEquals(json, writer.toString());
    final StringReader reader = new StringReader(json);
    AssertJUnit.assertEquals(result, (new JsonReader(reader)).read());
  }
}
