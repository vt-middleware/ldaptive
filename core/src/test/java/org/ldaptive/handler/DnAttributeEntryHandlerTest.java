/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link DnAttributeEntryHandler}.
 *
 * @author  Middleware Services
 */
public class DnAttributeEntryHandlerTest
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
          DnAttributeEntryHandler.builder().build(),
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
              LdapAttribute.builder().name("entryDN").values("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          DnAttributeEntryHandler.builder().build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("entryDN").values("cn=Homer Simpson").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("entryDN").values("cn=Homer Simpson").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          DnAttributeEntryHandler.builder().addIfExists(true).build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("entryDN").values("cn=Homer Simpson").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("entryDN")
                .values("cn=Homer Simpson", "cn=Homer Simpson,ou=People,dc=ldaptive,dc=org").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          DnAttributeEntryHandler.builder().dnAttributeName("myDN").build(),
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
              LdapAttribute.builder().name("myDN").values("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org").build(),
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
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
  public void apply(final DnAttributeEntryHandler handler, final LdapEntry actual, final LdapEntry expected)
  {
    assertThat(handler.apply(actual)).isEqualTo(expected);
  }
}
