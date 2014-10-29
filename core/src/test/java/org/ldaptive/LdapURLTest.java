/*
  $Id: LdapURLTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapURL}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
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
          new String[] {"directory.ldaptive.org"},
          new Integer[] {389},
        },
        new Object[] {
          new LdapURL("ldaps://directory.ldaptive.org"),
          new String[] {"directory.ldaptive.org"},
          new Integer[] {636},
        },
        new Object[] {
          new LdapURL("ldap://directory.ldaptive.org:10389"),
          new String[] {"directory.ldaptive.org"},
          new Integer[] {10389},
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org " +
            "ldap://directory2.ldaptive.org:10389"),
          new String[] {"directory1.ldaptive.org", "directory2.ldaptive.org", },
          new Integer[] {636, 10389, },
        },
      };
  }


  /**
   * @param  url  LdapUrl to test
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
    final String[] hostnames,
    final Integer[] ports)
    throws Exception
  {
    final Iterator<LdapURL.Entry> iter = url.getEntries().iterator();
    Assert.assertEquals(hostnames.length, url.size());
    for (int i = 0; i < hostnames.length; i++) {
      final LdapURL.Entry e = iter.next();
      Assert.assertEquals(hostnames[i], e.getHostname());
      Assert.assertEquals(ports[i], Integer.valueOf(e.getPort()));
    }
    Assert.assertEquals(hostnames[0], url.getEntry().getHostname());
    Assert.assertEquals(ports[0], Integer.valueOf(url.getEntry().getPort()));
    Assert.assertEquals(
      hostnames[hostnames.length - 1],
      url.getLastEntry().getHostname());
    Assert.assertEquals(
      ports[ports.length - 1],
      Integer.valueOf(url.getLastEntry().getPort()));
    Assert.assertEquals(hostnames, url.getEntriesAsString());
  }
}
