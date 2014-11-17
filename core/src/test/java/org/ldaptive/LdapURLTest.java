/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapURL}.
 *
 * @author  Middleware Services
 */
public class LdapURLTest
{


  /**
   * LDAP URL test data.
   *
   * @return  test data
   */
  @DataProvider(name = "urls")
  public Object[][] createURLs()
  {
    return
      new Object[][] {
        new Object[] {
          new LdapURL("ldap://directory.ldaptive.org"),
          new String[] {"ldap"},
          new String[] {"directory.ldaptive.org"},
          new Integer[] {389},
        },
        new Object[] {
          new LdapURL("ldaps://directory.ldaptive.org"),
          new String[] {"ldaps"},
          new String[] {"directory.ldaptive.org"},
          new Integer[] {636},
        },
        new Object[] {
          new LdapURL("ldap://directory.ldaptive.org:10389"),
          new String[] {"ldap"},
          new String[] {"directory.ldaptive.org"},
          new Integer[] {10389},
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org " +
            "ldap://directory2.ldaptive.org:10389"),
          new String[] {"ldaps", "ldap"},
          new String[] {"directory1.ldaptive.org", "directory2.ldaptive.org", },
          new Integer[] {636, 10389, },
        },
      };
  }


  /**
   * @param  url  LdapUrl to test
   * @param  schemes  to verify
   * @param  hostnames  to verify
   * @param  ports  to verify
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"ldapURL"},
    dataProvider = "urls"
  )
  public void testParsing(
    final LdapURL url,
    final String[] schemes,
    final String[] hostnames,
    final Integer[] ports)
    throws Exception
  {
    final Iterator<LdapURL.Entry> iter = url.getEntries().iterator();
    Assert.assertEquals(hostnames.length, url.size());
    for (int i = 0; i < hostnames.length; i++) {
      final LdapURL.Entry e = iter.next();
      Assert.assertEquals(schemes[i], e.getScheme());
      Assert.assertEquals(hostnames[i], e.getHostname());
      Assert.assertEquals(ports[i], Integer.valueOf(e.getPort()));
    }
    Assert.assertEquals(schemes[0], url.getEntry().getScheme());
    Assert.assertEquals(hostnames[0], url.getEntry().getHostname());
    Assert.assertEquals(ports[0], Integer.valueOf(url.getEntry().getPort()));
    Assert.assertEquals(
      schemes[schemes.length - 1],
      url.getLastEntry().getScheme());
    Assert.assertEquals(
      hostnames[hostnames.length - 1],
      url.getLastEntry().getHostname());
    Assert.assertEquals(
      ports[ports.length - 1],
      Integer.valueOf(url.getLastEntry().getPort()));
    Assert.assertEquals(hostnames, url.getHostnamesAsString());
  }
}
