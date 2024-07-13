/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link MergeAttributeEntryHandler}.
 *
 * @author  Middleware Services
 */
public class MergeAttributeEntryHandlerTest
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
          MergeAttributeEntryHandler.builder().mergeAttributeName("myAttr").attributeNames("cn", "sn").build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("myAttr").values("Homer", "Simpson").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          MergeAttributeEntryHandler.builder().mergeAttributeName("uid").attributeNames("cn", "sn").build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101", "Homer", "Simpson").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
      };
  }


  /**
   * @param  handler  to test
   * @param  actual  to handle
   * @param  expected  to compare
   */
  @Test(groups = "handler", dataProvider = "entries")
  public void apply(final MergeAttributeEntryHandler handler, final LdapEntry actual, final LdapEntry expected)
  {
    assertThat(handler.apply(actual)).isEqualTo(expected);
  }
}
