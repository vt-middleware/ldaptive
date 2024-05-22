/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CaseChangeEntryHandler}.
 *
 * @author  Middleware Services
 */
public class CaseChangeEntryHandlerTest
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
          CaseChangeEntryHandler.builder().build(),
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
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          CaseChangeEntryHandler.builder().dnCaseChange(CaseChangeEntryHandler.CaseChange.UPPER).build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
          LdapEntry.builder()
            .dn("CN=HOMER SIMPSON,OU=PEOPLE,DC=LDAPTIVE,DC=ORG")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("mail").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          CaseChangeEntryHandler.builder()
            .attributeNameCaseChange(CaseChangeEntryHandler.CaseChange.UPPER)
            .attributeNames("cn", "mail")
            .build(),
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
              LdapAttribute.builder().name("CN").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder().name("uid").values("101").build(),
              LdapAttribute.builder().name("MAIL").values("hsimpson@tv.com", "pieman@tv.com").build())
            .build(),
        },
        new Object[] {
          CaseChangeEntryHandler.builder()
            .attributeValueCaseChange(CaseChangeEntryHandler.CaseChange.LOWER)
            .attributeNames("cn", "sn")
            .build(),
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
              LdapAttribute.builder().name("cn").values("homer").build(),
              LdapAttribute.builder().name("sn").values("simpson").build(),
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
  public void apply(final CaseChangeEntryHandler handler, final LdapEntry actual, final LdapEntry expected)
  {
    Assert.assertEquals(handler.apply(actual), expected);
  }
}
