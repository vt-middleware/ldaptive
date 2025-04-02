/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
          "(&(lastName=Wallace)(sn=Wallace))",
          FilterTemplate.builder()
            .filter("(&(lastName={0})(sn={0}))")
            .parameter(0, "Wallace")
            .build(),
        },
        new Object[] {
          "(&(lastName=Wallace)(sn=Wallace))",
          FilterTemplate.builder()
            .filter("(&(lastName={sn})(sn={sn}))")
            .parameter("sn", "Wallace")
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
        new Object[] {
          "(&(givenName=B\\F0\\9F\\9C\\81\\7Bll\\7D))",
          FilterTemplate.builder()
            .filter("(&(givenName={firstname}))")
            .parameter("firstname", "B\uD83D\uDF01{ll}")
            .build(),
        },
        new Object[] {
          "(member=uid=username," +
            "ou=\\D0\\9B\\D0\\B0\\D0\\B1\\D0\\BE\\D1\\80\\D0\\B0\\D1\\82\\D0\\BE\\D1\\80\\D0\\B8\\D1\\8F," +
            "ou=\\D0\\A3\\D0\\BD\\D0\\B8\\D0\\B2\\D0\\B5\\D1\\80\\D1\\81\\D0\\B8\\D1\\82\\D0\\B5\\D1\\82," +
            "dc=company,dc=com)",
          FilterTemplate.builder()
            .filter("(member={dn})")
            .parameter("dn", "uid=username,ou=Лаборатория,ou=Университет,dc=company,dc=com")
            .build(),
        },
        new Object[] {
          "(member=uid=username," +
            "ou=\\D0\\9B\\D0\\B0\\D0\\B1\\D0\\BE\\D1\\80\\D0\\B0\\D1\\82\\D0\\BE\\D1\\80\\D0\\B8\\D1\\8F," +
            "ou=people,dc=company,dc=com)",
          FilterTemplate.builder()
            .filter("(member=uid=username,ou={ou},ou=people,dc=company,dc=com)")
            .parameter("ou", "Лаборатория".getBytes(StandardCharsets.UTF_8))
            .build(),
        },
        new Object[] {
          "(uid=\\7Buser\\7D)",
          FilterTemplate.builder()
            .filter("(uid={user})")
            .parameter("user", "{user}")
            .build(),
        },
        new Object[] {
          "(uid=foo\\7Buser\\7D\\2A)",
          FilterTemplate.builder()
            .filter("(uid={user})")
            .parameter("user", "foo{user}*")
            .build(),
        },
        new Object[] {
          "(|(uid=\\7Bdn\\7D)(mail=\\7Bdn\\7D))",
          FilterTemplate.builder()
            .filter("(|(uid={user})(mail={user}))")
            .parameter("user", "{dn}")
            .parameter("dn", "uid=12345")
            .build(),
        },
        new Object[] {
          "(|(uid=\\7Buser\\7D)(mail=\\7Buser\\7D))",
          FilterTemplate.builder()
            .filter("(|(uid={user})(mail={user}))")
            .parameter("user", "{user}")
            .build(),
        },
        new Object[] {
          "(&(givenName=\\7Bsn\\7D)(sn=\\7Bgn\\7D))",
          FilterTemplate.builder()
            .filter("(&(givenName={gn})(sn={sn}))")
            .parameter("gn", "{sn}")
            .parameter("sn", "{gn}")
            .build(),
        },
        new Object[] {
          "(&(givenName=\\7Bsn\\7D)(sn=\\2A\\7Bgn\\7Dbar))",
          FilterTemplate.builder()
            .filter("(&(givenName={gn})(sn={sn}))")
            .parameter("gn", "{sn}")
            .parameter("sn", "*{gn}bar")
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
    assertThat(filter.format()).isEqualTo(encodedFilter);
  }
}
