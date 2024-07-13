/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import org.ldaptive.dn.Dn;
import org.ldaptive.dn.NameValue;
import org.ldaptive.dn.RDn;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link DNSDomainFunction}.
 *
 * @author  Middleware Services
 */
public class DNSDomainFunctionTest
{


  /**
   * DN test data.
   *
   * @return  test data
   */
  @DataProvider(name = "DNs")
  public Object[][] createDNs()
  {
    return
      new Object[][]{
        new Object[]{
          Dn.builder().add("cn=John Doe").add("ou=accounting").add("dc=example").add("dc=net").build(),
          "example.net",
        },
        new Object[]{
          Dn.builder().add("DC=ldaptive").add("DC=org").build(),
          "ldaptive.org",
        },
        new Object[]{
          Dn.builder().add("DC=ldap").add("ou=example").add("DC=ldaptive").add("DC=org").build(),
          "ldaptive.org",
        },
        new Object[]{
          Dn.builder()
            .add("CN=test")
            .add(new RDn(new NameValue("DC", "ldap1"), new NameValue("DC", "ldap2")))
            .add("DC=ldaptive")
            .add("DC=org").build(),
          "ldaptive.org",
        },
        new Object[]{
          Dn.builder().add("DC=ldap").add("DC=").add("DC=ldaptive").add("DC=org").build(),
          "ldap.ldaptive.org",
        },
        new Object[]{
          Dn.builder().add("DC=.").add("DC=ldap").add("DC=ldaptive").add("DC=org").build(),
          "ldap.ldaptive.org",
        },
      };
  }


  /**
   * @param  dn  DN to parse
   * @param  domain  to match against
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "DNs")
  public void testApply(final Dn dn, final String domain)
  {
    final String dnDomain = new DNSDomainFunction().apply(dn);
    assertThat(dnDomain).isEqualTo(domain);
  }
}
