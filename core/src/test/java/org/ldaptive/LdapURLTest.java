/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "urls")
  public Object[][] createURLs()
  {
    return
      new Object[][] {
        new Object[] {"ldap://[::1]", "ldap", "::1", 389, },
        new Object[] {"ldap://[::1]/", "ldap", "::1", 389, },
        new Object[] {"ldap://[::1]:9876", "ldap", "::1", 9876, },
        new Object[] {"ldap://[::1]:9876/", "ldap", "::1", 9876, },
        new Object[] {"ldap://ldap1.example.net/o=University%20of%20Michigan,c=US", "ldap", "ldap1.example.net", 389, },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US?postalAddress",
          "ldap",
          "ldap1.example.net",
          389,
        },
        new Object[] {
          "ldap://ldap1.example.net:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
          "ldap",
          "ldap1.example.net",
          6666,
        },
        new Object[] {
          "LDAP://ldap1.example.com/c=GB?objectClass?ONE",
          "ldap",
          "ldap1.example.com",
          389,
        },
        new Object[] {
          "ldap://ldap2.example.com/o=Question%3f,c=US?mail",
          "ldap",
          "ldap2.example.com",
          389,
        },
        new Object[] {
          "ldap://ldap3.example.com/o=Babsco,c=US???(four-octet=%5c00%5c00%5c00%5c04)",
          "ldap",
          "ldap3.example.com",
          389,
        },
        new Object[] {
          "ldap://ldap.example.com/o=An%20Example%5C2C%20Inc.,c=US",
          "ldap",
          "ldap.example.com",
          389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org",
          "ldap",
          "directory.ldaptive.org",
          389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org/",
          "ldap",
          "directory.ldaptive.org",
          389,
        },
        new Object[] {
          "ldaps://directory.ldaptive.org",
          "ldaps",
          "directory.ldaptive.org",
          636,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn?",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one?",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(cn=foo bar)",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org???(uid=dfisher)",
          "ldap",
          "directory.ldaptive.org",
          10389,
        },
        new Object[] {
          "ldap://192.168.10.3",
          "ldap",
          "192.168.10.3",
          389,
        },
        new Object[] {
          "ldaps://192.168.10.3",
          "ldaps",
          "192.168.10.3",
          636,
        },
        new Object[] {
          "ldaps://192.168.10.3:10636",
          "ldaps",
          "192.168.10.3",
          10636,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org",
          "ldap",
          "192.168.10.3",
          10389,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn",
          "ldap",
          "192.168.10.3",
          10389,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn",
          "ldap",
          "192.168.10.3",
          10389,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one",
          "ldap",
          "192.168.10.3",
          10389,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          "ldap",
          "192.168.10.3",
          10389,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          389,
        },
        new Object[] {
          "ldaps://[2607:b400:90:6800:2000:0:0:64]",
          "ldaps",
          "2607:b400:90:6800:2000:0:0:64",
          636,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          10389,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          10389,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          10389,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          10389,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          10389,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          "ldap",
          "2607:b400:90:6800:2000:0:0:64",
          10389,
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * LDAP URL test data.
   *
   * @return  test data
   */
  @DataProvider(name = "invalid-urls")
  public Object[][] invalidURLs()
  {
    return
      new Object[][] {
        new Object[] {null, },
        new Object[] {"", },
        new Object[] {"foo", },
        new Object[] {"foo://", },
        new Object[] {"ldap", },
        new Object[] {"ldap://", },
        new Object[] {"ldap:///", },
        new Object[] {"ldap:///?", },
        new Object[] {"ldap:///??", },
        new Object[] {"ldap:///???", },
        new Object[] {"ldap://:1234/?", },
        new Object[] {"ldap://:1234/??", },
        new Object[] {"ldap://:1234/???", },
        new Object[] {"ldap://:1234", },
        new Object[] {"ldap://:1234/", },
        new Object[] {"ldaps://",  },
        new Object[] {"ldaps://:1234", },
        new Object[] {"ldaps:///", },
        new Object[] {"ldap://:389/??one?(givenName=bob)", },
        new Object[] {"ldaps://:636/??one?(givenName=bob)", },
        new Object[] {"ldap:///o=University%20of%20Michigan,c=US", },
        new Object[] {"ldap://:notnumber", },
        new Object[] {"ldap://ldaphost:notnumber", },
        new Object[] {"ldap://ldaphost:-1", },
        new Object[] {"ldap://ldaphost:70000", },
        new Object[] {"ldap://[]", },
        new Object[] {"ldap://[]:notnumber", },
        new Object[] {"ldap://[]:10389", },
        new Object[] {"ldap://[::1]:notnumber", },
        new Object[] {"ldap://[::1:10389", },
        new Object[] {"ldap://[::1]10389", },
        new Object[] {"ldap:///?,", },
        new Object[] {"ldap:///? ,", },
        new Object[] {"ldap:///?,?", },
        new Object[] {"ldap:///?,cn,sn", },
        new Object[] {"ldap:///?cn,,sn", },
        new Object[] {"ldap:///?cn,sn,", },
        new Object[] {"ldap:///?cn,sn, ", },
        new Object[] {"ldap:///?cn,,sn?", },
        new Object[] {"ldap:///??foo", },
        new Object[] {"ldap:///??foo?", },
        new Object[] {"ldap:///???foo", },
        new Object[] {"ldap:///+", },
        new Object[] {"ldap:///dc=%", },
        new Object[] {"ldap:///dc=%1", },
        new Object[] {"ldap:///dc=%y", },
        new Object[] {"ldap:///dc=%oy", },
        new Object[] {"ldap:///dc=%aa%", },
      };
  }


  /**
   * @param  actual  url to parse
   * @param  scheme  to compare
   * @param  hostname  to compare
   * @param  port  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "urls")
  public void testValid(final String actual, final String scheme, final String hostname, final int port)
    throws Exception
  {
    final LdapURL url = new LdapURL(actual);
    Assert.assertEquals(url.getScheme(), scheme);
    Assert.assertEquals(url.getHostname(), hostname);
    Assert.assertEquals(url.getPort(), port);
    if (hostname != null) {
      Assert.assertEquals(url.getHostnameWithPort(), hostname + ":" + port);
      Assert.assertEquals(url.getHostnameWithSchemeAndPort(), scheme + "://" + hostname + ":" + port);
    }
  }


  /**
   * @param  url  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "invalid-urls")
  public void testInvalid(final String url)
    throws Exception
  {
    try {
      new LdapURL(url);
      Assert.fail("Should have thrown exception");
    } catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }
}
