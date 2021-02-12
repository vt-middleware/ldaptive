/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResponse;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link MergeResultHandler}.
 *
 * @author  Miguel Martinez de Espronceda
 */
public class MergeResultHandlerTest
{


  /**
   * Entry test data.
   *
   * @return  test data
   */
  @DataProvider(name = "entries")
  public Object[][] createEntries()
  {
    return
      new Object[][] {
        new Object[] {
          new LdapEntry[] {
            LdapEntry.builder()
              .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("cn").values("Homer").build(),
                LdapAttribute.builder().name("sn").values("Simpson").build(),
                LdapAttribute.builder().name("uid").values("101").build(),
                LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
              .build(),
            LdapEntry.builder()
              .dn("cn=Bart Simpson,ou=People,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("sn").values("Simpson").build(),
                LdapAttribute.builder().name("cn").values("Bart").build(),
                LdapAttribute.builder().name("uid").values("102").build(),
                LdapAttribute.builder().name("mail").values("bsimpson@tv.com", "bartman@tv.com").build())
              .build(),
            LdapEntry.builder()
              .dn("cn=Lisa Simpson,ou=People,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("cn").values("Lisa").build(),
                LdapAttribute.builder().name("uid").values("103").build(),
                LdapAttribute.builder().name("sn").values("Simpson").build())
              .build(),
          },
          new LdapEntry[] {
            LdapEntry.builder()
              .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
              .attributes(
                LdapAttribute.builder().name("cn").values("Homer", "Bart", "Lisa").build(),
                LdapAttribute.builder().name("mail")
                  .values("hsimpson@tv.com", "pieman@tv.com", "bartman@tv.com", "bsimpson@tv.com").build(),
                LdapAttribute.builder().name("sn").values("Simpson").build(),
                LdapAttribute.builder().name("uid").values("101", "102", "103").build())
              .build(),
          },
        },
      };
  }


  @Test(groups = "handlers", dataProvider = "entries")
  public void apply(final LdapEntry[] actual, final LdapEntry[] expected)
  {
    final MergeResultHandler handler = new MergeResultHandler();
    final SearchResponse response = new SearchResponse();
    response.addEntries(actual);
    Assert.assertEquals(
      handler.apply(response).getEntries(),
      Stream.of(expected).collect(Collectors.toCollection(LinkedHashSet::new)));
  }
}
