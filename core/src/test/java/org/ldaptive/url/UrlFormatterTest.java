/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link UrlFormatter} implementations.
 *
 * @author  Middleware Services
 */
public class UrlFormatterTest
{


  /**
   * LDAP URL test data.
   *
   * @return  test data
   *
   * @throws  Exception  if test data creation fails
   */
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "urls")
  public Object[][] createURLs()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {
          "ldap://",
          "ldap://:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap:///",
          "ldap://:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap:///?",
          "ldap://:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap:///??",
          "ldap://:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap:///???",
          "ldap://:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://:1234/?",
          "ldap://:1234/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://:1234/??",
          "ldap://:1234/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://:1234/???",
          "ldap://:1234/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://:1234",
          "ldap://:1234/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://:1234/",
          "ldap://:1234/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[::1]",
          "ldap://[::1]:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[::1]/",
          "ldap://[::1]:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[::1]:9876",
          "ldap://[::1]:9876/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[::1]:9876/",
          "ldap://[::1]:9876/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps://",
          "ldaps://:636/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps://:1234",
          "ldaps://:1234/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps:///",
          "ldaps://:636/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://:389/??one?(givenName=bob)",
          "ldap://:389/??one?(givenName=bob)",
        },
        new Object[] {
          "ldaps://:636/??one?(givenName=bob)",
          "ldaps://:636/??one?(givenName=bob)",
        },
        new Object[] {
          "ldap:///o=University%20of%20Michigan,c=US",
          "ldap://:389/o=University%20of%20Michigan,c=US??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US",
          "ldap://ldap1.example.net:389/o=University%20of%20Michigan,c=US??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US?postalAddress",
          "ldap://ldap1.example.net:389/o=University%20of%20Michigan,c=US?postalAddress?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://ldap1.example.net:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
          "ldap://ldap1.example.net:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
        },
        new Object[] {
          "LDAP://ldap1.example.com/c=GB?objectClass?ONE",
          "ldap://ldap1.example.com:389/c=GB?objectClass?one?(objectClass=*)",
        },
        new Object[] {
          "ldap://ldap2.example.com/o=Question%3f,c=US?mail",
          "ldap://ldap2.example.com:389/o=Question%3F,c=US?mail?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://ldap3.example.com/o=Babsco,c=US???(four-octet=%5c00%5c00%5c00%5c04)",
          "ldap://ldap3.example.com:389/o=Babsco,c=US??base?(four-octet=%5C00%5C00%5C00%5C04)",
        },
        new Object[] {
          "ldap://ldap.example.com/o=An%20Example%5C2C%20Inc.,c=US",
          "ldap://ldap.example.com:389/o=An%20Example%5C2C%20Inc.,c=US??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org",
          "ldap://directory.ldaptive.org:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org/",
          "ldap://directory.ldaptive.org:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps://directory.ldaptive.org",
          "ldaps://directory.ldaptive.org:636/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389",
          "ldap://directory.ldaptive.org:10389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/",
          "ldap://directory.ldaptive.org:10389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn?",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one?",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(objectClass=*)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(cn=foo bar)",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(cn=foo%20bar)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org???(uid=dfisher)",
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??base?(uid=dfisher)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/ou=Лаборатория???(flag=Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F)",
          "ldap://directory.ldaptive.org:10389/ou=%D0%9B%D0%B0%D0%B1%D0%BE%D1%80%D0%B0%D1%82%D0%BE%D1%80%D0%B8%D1%8F" +
            "??base?(flag=Pirate%20Flag%20%3F%3F%E2%80%8D%E2%98%A0%EF%B8%8F)",
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/ou=%D0%9B%D0%B0%D0%B1%D0%BE%D1%80%D0%B0%D1%82%D0%BE%D1%80%D0%B8%D1%8F" +
            "??base?(flag=Pirate%20Flag%20%3F%3F%E2%80%8D%E2%98%A0%EF%B8%8F)",
          "ldap://directory.ldaptive.org:10389/ou=%D0%9B%D0%B0%D0%B1%D0%BE%D1%80%D0%B0%D1%82%D0%BE%D1%80%D0%B8%D1%8F" +
            "??base?(flag=Pirate%20Flag%20%3F%3F%E2%80%8D%E2%98%A0%EF%B8%8F)",
        },
        new Object[] {
          "ldap://192.168.10.3",
          "ldap://192.168.10.3:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps://192.168.10.3",
          "ldaps://192.168.10.3:636/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps://192.168.10.3:10636",
          "ldaps://192.168.10.3:10636/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org",
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn",
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn",
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one",
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one?(objectClass=*)",
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldaps://[2607:b400:90:6800:2000:0:0:64]",
          "ldaps://[2607:b400:90:6800:2000:0:0:64]:636/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org??base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?base?(objectClass=*)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one?(objectClass=*)",
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * @param  actual  url to parse
   * @param  expected  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "urls")
  public void testMinimalFormatting(final String actual, final String expected)
    throws Exception
  {
    final Url url = new Url(actual);
    Assert.assertEquals(url.format(), expected);
  }
}
