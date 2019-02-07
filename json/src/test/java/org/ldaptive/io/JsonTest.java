/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.io.StringReader;
import java.io.StringWriter;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
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
          SearchResponse.builder()
          .entry(
            LdapEntry.builder()
              .dn("uid=818037,ou=people,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("departmentNumber").values("066103").build(),
                LdapAttribute.builder().name("givenName").values("Daniel", "Dan").build(),
                LdapAttribute.builder().name("sn").values("Fisher").build())
            .build())
          .reference(
            SearchResultReference.builder()
              .uris("ldap://localhost:389/dc=ldaptive,dc=org??sub")
            .build(),
            SearchResultReference.builder()
              .uris(
                "ldap://localhost:389/dc=ldaptive,dc=org??sub",
                "ldap://directory.ldaptive.oeg:10389/dc=ldaptive,dc=org??sub")
            .build())
          .build(),
          "[{\"dn\":\"uid=818037,ou=people,dc=ldaptive,dc=org\"," +
              "\"departmentNumber\":[\"066103\"]," +
              "\"givenName\":[\"Daniel\",\"Dan\"]," +
              "\"sn\":[\"Fisher\"]}," +
              "{\"ref\":[\"ldap://localhost:389/dc=ldaptive,dc=org??sub\"]}," +
              "{\"ref\":[\"ldap://localhost:389/dc=ldaptive,dc=org??sub\"," +
              "\"ldap://directory.ldaptive.oeg:10389/dc=ldaptive,dc=org??sub\"]}]",
        },
        {
          SearchResponse.builder()
          .entry(
            LdapEntry.builder()
              .dn("uid=1095747,ou=people,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("uid").values("1095747").build(),
                LdapAttribute.builder().name("givenName").values("Robert", "Bob").build(),
                LdapAttribute.builder().name("sn").values("Jones").build())
            .build(),
            LdapEntry.builder()
              .dn("uid=1141837,ou=people,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("uid").values("1141837").build(),
                LdapAttribute.builder().name("givenName").values("William", "Bill").build(),
                LdapAttribute.builder().name("sn").values("Smith").build())
            .build(),
            LdapEntry.builder()
              .dn("uid=1145718,ou=people,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("uid").values("1145718").build(),
                LdapAttribute.builder().name("givenName").values("Thomas", "Tom").build(),
                LdapAttribute.builder().name("sn").values("Johnson").build())
            .build(),
            LdapEntry.builder()
              .dn("uid=1152120,ou=people,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("uid").values("1152120").build(),
                LdapAttribute.builder().name("givenName").values("David", "Dave").build(),
                LdapAttribute.builder().name("sn").values("Brown").build())
            .build(),
            LdapEntry.builder()
              .dn("uid=818037,ou=people,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("uid").values("818037").build(),
                LdapAttribute.builder().name("givenName").values("Joseph", "Joe").build(),
                LdapAttribute.builder().name("sn").values("Anderson").build())
            .build())
          .build(),
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
  @Test(groups = "io", dataProvider = "search-data")
  public void writer(final SearchResponse result, final String json)
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
  @Test(groups = "io", dataProvider = "search-data")
  public void reader(final SearchResponse result, final String json)
    throws Exception
  {
    final StringReader reader = new StringReader(json);
    AssertJUnit.assertEquals(result, (new JsonReader(reader)).read());
  }
}
