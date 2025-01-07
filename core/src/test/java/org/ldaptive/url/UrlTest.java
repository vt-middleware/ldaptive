/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.url;

import org.ldaptive.SearchScope;
import org.ldaptive.dn.Dn;
import org.ldaptive.filter.EqualityFilter;
import org.ldaptive.filter.Filter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link Url}.
 *
 * @author  Middleware Services
 */
public class UrlTest
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
          new Url(
            "ldap",
            null,
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap:///",
          new Url(
            "ldap",
            null,
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap:///?",
          new Url(
            "ldap",
            null,
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap:///??",
          new Url(
            "ldap",
            null,
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap:///???",
          new Url(
            "ldap",
            null,
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://:1234/?",
          new Url(
            "ldap",
            null,
            1234,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://:1234/??",
          new Url(
            "ldap",
            null,
            1234,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://:1234/???",
          new Url(
            "ldap",
            null,
            1234,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://:1234",
          new Url(
            "ldap",
            null,
            1234,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://:1234/",
          new Url(
            "ldap",
            null,
            1234,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[::1]",
          new Url(
            "ldap",
            "::1",
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[::1]/",
          new Url(
            "ldap",
            "::1",
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[::1]:9876",
          new Url(
            "ldap",
            "::1",
            9876,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[::1]:9876/",
          new Url(
            "ldap",
            "::1",
            9876,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps://",
          new Url(
            "ldaps",
            null,
            Url.DEFAULT_LDAPS_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps://:1234",
          new Url(
            "ldaps",
            null,
            1234,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps:///",
          new Url(
            "ldaps",
            null,
            Url.DEFAULT_LDAPS_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://:389/??one?(givenName=bob)",
          new Url(
            "ldap",
            null,
            389,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            SearchScope.ONELEVEL,
            "(givenName=bob)"),
          Url.DEFAULT_PARSED_BASE_DN,
          new EqualityFilter("givenName", "bob"),
        },
        new Object[] {
          "ldaps://:636/??one?(givenName=bob)",
          new Url(
            "ldaps",
            null,
            636,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            SearchScope.ONELEVEL,
            "(givenName=bob)"),
          Url.DEFAULT_PARSED_BASE_DN,
          new EqualityFilter("givenName", "bob"),
        },
        new Object[] {
          "ldap:///o=University%20of%20Michigan,c=US",
          new Url(
            "ldap",
            null,
            Url.DEFAULT_LDAP_PORT,
            "o=University of Michigan,c=US",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("o=University of Michigan,c=US"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US",
          new Url(
            "ldap",
            "ldap1.example.net",
            Url.DEFAULT_LDAP_PORT,
            "o=University of Michigan,c=US",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("o=University of Michigan,c=US"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US?postalAddress",
          new Url(
            "ldap",
            "ldap1.example.net",
            Url.DEFAULT_LDAP_PORT,
            "o=University of Michigan,c=US",
            new String[] {"postalAddress"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("o=University of Michigan,c=US"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://ldap1.example.net:6666/o=University%20of%20Michigan,c=US??sub?(cn=Babs%20Jensen)",
          new Url(
            "ldap",
            "ldap1.example.net",
            6666,
            "o=University of Michigan,c=US",
            Url.DEFAULT_ATTRIBUTES,
            SearchScope.SUBTREE,
            "(cn=Babs Jensen)"),
          new Dn("o=University of Michigan,c=US"),
          new EqualityFilter("cn", "Babs Jensen"),
        },
        new Object[] {
          "LDAP://ldap1.example.com/c=GB?objectClass?ONE",
          new Url(
            "ldap",
            "ldap1.example.com",
            Url.DEFAULT_LDAP_PORT,
            "c=GB",
            new String[] {"objectClass"},
            SearchScope.ONELEVEL,
            Url.DEFAULT_FILTER),
          new Dn("c=GB"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://ldap2.example.com/o=Question%3f,c=US?mail",
          new Url(
            "ldap",
            "ldap2.example.com",
            Url.DEFAULT_LDAP_PORT,
            "o=Question?,c=US",
            new String[] {"mail"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("o=Question?,c=US"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://ldap3.example.com/o=Babsco,c=US???(four-octet=%5c00%5c00%5c00%5c04)",
          new Url(
            "ldap",
            "ldap3.example.com",
            Url.DEFAULT_LDAP_PORT,
            "o=Babsco,c=US",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            "(four-octet=\\00\\00\\00\\04)"),
          new Dn("o=Babsco,c=US"),
          new EqualityFilter("four-octet", new byte[] {0x00, 0x00, 0x00, 0x04}),
        },
        new Object[] {
          "ldap://ldap.example.com/o=An%20Example%5C2C%20Inc.,c=US",
          new Url(
            "ldap",
            "ldap.example.com",
            Url.DEFAULT_LDAP_PORT,
            "o=An Example\\2C Inc.,c=US",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("o=An Example\\2C Inc.,c=US"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org/",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps://directory.ldaptive.org",
          new Url(
            "ldaps",
            "directory.ldaptive.org",
            Url.DEFAULT_LDAPS_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn?",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            SearchScope.ONELEVEL,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org??one?",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            SearchScope.ONELEVEL,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(uid=dfisher)"),
          new Dn("dc=ldaptive,dc=org"),
          new EqualityFilter("uid", "dfisher"),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn?one?(cn=foo bar)",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(cn=foo bar)"),
          new Dn("dc=ldaptive,dc=org"),
          new EqualityFilter("cn", "foo bar"),
        },
        new Object[] {
          "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org???(uid=dfisher)",
          new Url(
            "ldap",
            "directory.ldaptive.org",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            "(uid=dfisher)"),
          new Dn("dc=ldaptive,dc=org"),
          new EqualityFilter("uid", "dfisher"),
        },
        new Object[] {
          "ldap://192.168.10.3",
          new Url(
            "ldap",
            "192.168.10.3",
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps://192.168.10.3",
          new Url(
            "ldaps",
            "192.168.10.3",
            Url.DEFAULT_LDAPS_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps://192.168.10.3:10636",
          new Url(
            "ldaps",
            "192.168.10.3",
            10636,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org",
          new Url(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn",
          new Url(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn",
          new Url(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one",
          new Url(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://192.168.10.3:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          new Url(
            "ldap",
            "192.168.10.3",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(uid=dfisher)"),
          new Dn("dc=ldaptive,dc=org"),
          new EqualityFilter("uid", "dfisher"),
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            Url.DEFAULT_LDAP_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldaps://[2607:b400:90:6800:2000:0:0:64]",
          new Url(
            "ldaps",
            "2607:b400:90:6800:2000:0:0:64",
            Url.DEFAULT_LDAPS_PORT,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            Url.DEFAULT_BASE_DN,
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          Url.DEFAULT_PARSED_BASE_DN,
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            Url.DEFAULT_ATTRIBUTES,
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            Url.DEFAULT_SCOPE,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            Url.DEFAULT_FILTER),
          new Dn("dc=ldaptive,dc=org"),
          Url.DEFAULT_PARSED_FILTER,
        },
        new Object[] {
          "ldap://[2607:b400:90:6800:2000:0:0:64]:10389/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)",
          new Url(
            "ldap",
            "2607:b400:90:6800:2000:0:0:64",
            10389,
            "dc=ldaptive,dc=org",
            new String[] {"cn", "sn"},
            SearchScope.ONELEVEL,
            "(uid=dfisher)"),
          new Dn("dc=ldaptive,dc=org"),
          new EqualityFilter("uid", "dfisher"),
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
        //new Object[] {"ldap:///?cn,sn,", }, regex parser allows trailing comma
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
        new Object[] {"ldap:///a=#666666666666666666666666666666666666666666666666666666666666666666", },
      };
  }


  /**
   * @param  actual  url to parse
   * @param  expected  to compare
   * @param  baseDn  that should match parsed DN
   * @param  filter  that should match parsed filter
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "urls")
  public void testDefaultParsing(final String actual, final Url expected, final Dn baseDn, final Filter filter)
    throws Exception
  {
    final DefaultUrlParser parser = new DefaultUrlParser();
    final Url url = parser.parse(actual);
    compareEntries(url, expected);
    assertThat(url.getParsedBaseDn()).isEqualTo(baseDn);
    assertThat(url.getParsedFilter()).isEqualTo(filter);
  }


  /**
   * @param  url  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "invalid-urls")
  public void testInvalidDefaultParsing(final String url)
    throws Exception
  {
    final DefaultUrlParser parser = new DefaultUrlParser();
    try {
      parser.parse(url);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }


  /**
   * @param  actual  url to parse
   * @param  expected  to compare
   * @param  baseDn  that should match parsed DN
   * @param  filter  that should match parsed filter
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "urls")
  public void testRegexParsing(final String actual, final Url expected, final Dn baseDn, final Filter filter)
    throws Exception
  {
    final RegexUrlParser parser = new RegexUrlParser();
    final Url url = parser.parse(actual);
    compareEntries(url, expected);
    assertThat(url.getParsedBaseDn()).isEqualTo(baseDn);
    assertThat(url.getParsedFilter()).isEqualTo(filter);
  }


  /**
   * @param  url  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ldapURL", dataProvider = "invalid-urls")
  public void testInvalidRegexParsing(final String url)
    throws Exception
  {
    final RegexUrlParser parser = new RegexUrlParser();
    try {
      parser.parse(url);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
  }


  /**
   * Compare all the properties of the supplied entries.
   *
   * @param  entry1  to compare
   * @param  entry2  to compare
   */
  private void compareEntries(final Url entry1, final Url entry2)
  {
    assertThat(entry1.getScheme()).isEqualTo(entry2.getScheme());
    assertThat(entry1.getHostname()).isEqualTo(entry2.getHostname());
    assertThat(entry1.getPort()).isEqualTo(entry2.getPort());
    assertThat(entry1.getBaseDn()).isEqualTo(entry2.getBaseDn());
    assertThat(entry1.getAttributes()).isEqualTo(entry2.getAttributes());
    assertThat(entry1.getScope()).isEqualTo(entry2.getScope());
    assertThat(entry1.getFilter()).isEqualTo(entry2.getFilter());
  }
}
