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
        new Object[] {
          "ldap://",
          new LdapURL(
            "ldap",
            null,
            389,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldaps://",
          new LdapURL(
            "ldaps",
            null,
            636,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap:///o=University%20of%20Michigan,c=US",
          new LdapURL(
            "ldap",
            null,
            389,
            "o=University of Michigan,c=US",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US",
          new LdapURL(
            "ldap",
            "ldap1.example.net",
            389,
            "o=University of Michigan,c=US",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US?postalAddress",
          new LdapURL(
            "ldap",
            "ldap1.example.net",
            389,
            "o=University of Michigan,c=US",
            new String[] {"postalAddress"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://ldap1.example.net:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
          new LdapURL(
            "ldap",
            "ldap1.example.net",
            6666,
            "o=University of Michigan,c=US",
            LdapURL.DEFAULT_ATTRIBUTES,
            SearchScope.SUBTREE,
            "(cn=Babs Jensen)"),
        },
        new Object[] {
          "LDAP://ldap1.example.com/c=GB?objectClass?ONE",
          new LdapURL(
            "ldap",
            "ldap1.example.com",
            389,
            "c=GB",
            new String[] {"objectClass"},
            SearchScope.ONELEVEL,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://ldap2.example.com/o=Question%3f,c=US?mail",
          new LdapURL(
            "ldap",
            "ldap2.example.com",
            389,
            "o=Question?,c=US",
            new String[] {"mail"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://ldap2.example.com/o=Question%3f,c=US?mail",
          new LdapURL(
            "ldap",
            "ldap2.example.com",
            389,
            "o=Question?,c=US",
            new String[] {"mail"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://ldap3.example.com/o=Babsco,c=US???(four-octet=%5c00%5c00%5c00%5c04)",
          new LdapURL(
            "ldap",
            "ldap3.example.com",
            389,
            "o=Babsco,c=US",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            "(four-octet=\\00\\00\\00\\04)"),
        },
        new Object[] {
          "ldap://ldap.example.com/o=An%20Example%5C2C%20Inc.,c=US",
          new LdapURL(
            "ldap",
            "ldap.example.com",
            389,
            "o=An Example\\2C Inc.,c=US",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            389,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldaps://directory.ldaptive.org",
          new LdapURL(
            "ldaps",
            "directory.ldaptive.org",
            636,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            10389,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          new LdapURL(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(uid=dfisher)"),
        },
        new Object[] {
          "ldap://192.168.10.3",
          new LdapURL(
            "ldap",
            "192.168.10.3",
            389,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldaps://192.168.10.3",
          new LdapURL(
            "ldaps",
            "192.168.10.3",
            636,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldaps://192.168.10.3:10636",
          new LdapURL(
            "ldaps",
            "192.168.10.3",
            10636,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org",
          new LdapURL(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn",
          new LdapURL(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn",
          new LdapURL(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one",
          new LdapURL(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          new LdapURL(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(uid=dfisher)"),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            389,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldaps://[2607:b400:90:6800:2000:0:0:64]",
          new LdapURL(
            "ldaps",
            "2607:b400:90:6800:2000:0:0:64",
            636,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            LdapURL.DEFAULT_BASE_DN,
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            LdapURL.DEFAULT_ATTRIBUTES,
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            LdapURL.DEFAULT_SCOPE,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            LdapURL.DEFAULT_FILTER),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          new LdapURL(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(uid=dfisher)"),
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
  public void testParsing(final String actual, final LdapURL expected)
    throws Exception
  {
    final LdapURL url = new LdapURL(actual);
    compareEntries(url, expected);

    Assert.assertEquals(url.getHostname(), expected.getHostname());
    Assert.assertEquals(url.getHostnameWithPort(), expected.getHostnameWithPort());
    Assert.assertEquals(url.getHostnameWithSchemeAndPort(), expected.getHostnameWithSchemeAndPort());
    Assert.assertEquals(url.getUrl(), expected.getUrl());
  }


  /**
   * Compare all the properties of the supplied entries.
   *
   * @param  entry1  to compare
   * @param  entry2  to compare
   */
  private void compareEntries(final LdapURL entry1, final LdapURL entry2)
  {
    Assert.assertEquals(entry1.getScheme(), entry2.getScheme());
    Assert.assertEquals(entry1.getHostname(), entry2.getHostname());
    Assert.assertEquals(entry1.getPort(), entry2.getPort());
    Assert.assertEquals(entry1.getBaseDn(), entry2.getBaseDn());
    Assert.assertEquals(entry1.getAttributes(), entry2.getAttributes());
    Assert.assertEquals(entry1.getScope(), entry2.getScope());
    Assert.assertEquals(entry1.getFilter(), entry2.getFilter());
  }
}
