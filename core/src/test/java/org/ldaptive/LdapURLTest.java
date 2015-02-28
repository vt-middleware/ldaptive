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
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "urls")
  public Object[][] createURLs()
  {
    return
      new Object[][] {
        new Object[] {
          new LdapURL("ldap://"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              null,
              389,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldaps://"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              null,
              636,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldap:///o=University%20of%20Michigan,c=US"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              null,
              389,
              "o=University of Michigan,c=US",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap1.example.net",
              389,
              "o=University of Michigan,c=US",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://ldap1.example.net/o=University%20of%20Michigan,c=US" +
              "?postalAddress"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap1.example.net",
              389,
              "o=University of Michigan,c=US",
              new String[] {"postalAddress"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://ldap1.example.net:6666/o=University%20of%20Michigan,c=US" +
              "??sub?(cn=Babs%20Jensen)"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap1.example.net",
              6666,
              "o=University of Michigan,c=US",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              SearchScope.SUBTREE,
              new SearchFilter("(cn=Babs Jensen)")),
          },
        },
        new Object[] {
          new LdapURL("LDAP://ldap1.example.com/c=GB?objectClass?ONE"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap1.example.com",
              389,
              "c=GB",
              new String[] {"objectClass"},
              SearchScope.ONELEVEL,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldap://ldap2.example.com/o=Question%3f,c=US?mail"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap2.example.com",
              389,
              "o=Question?,c=US",
              new String[] {"mail"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldap://ldap2.example.com/o=Question%3f,c=US?mail"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap2.example.com",
              389,
              "o=Question?,c=US",
              new String[] {"mail"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://ldap3.example.com/o=Babsco,c=US" +
              "???(four-octet=%5c00%5c00%5c00%5c04)"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap3.example.com",
              389,
              "o=Babsco,c=US",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              new SearchFilter("(four-octet=\\00\\00\\00\\04)")),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://ldap.example.com/o=An%20Example%5C2C%20Inc.,c=US"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "ldap.example.com",
              389,
              "o=An Example\\2C Inc.,c=US",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldap://directory.ldaptive.org"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              389,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldaps://directory.ldaptive.org"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              "directory.ldaptive.org",
              636,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL("ldap://directory.ldaptive.org:10389"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              10389,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org?cn,sn"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn", "sn"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org" +
              "?cn,sn?one"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn", "sn"},
              SearchScope.ONELEVEL,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldap://directory.ldaptive.org:10389/dc=ldaptive,dc=org" +
              "?cn,sn?one?(uid=dfisher)"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldap",
              "directory.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn", "sn"},
              SearchScope.ONELEVEL,
              new SearchFilter("(uid=dfisher)")),
          },
        },
        // multiple URLs
        new Object[] {
          new LdapURL("ldaps:// ldap://"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              null,
              636,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
            new LdapURL.Entry(
              "ldap",
              null,
              389,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org " +
            "ldap://directory2.ldaptive.org:10389"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              "directory1.ldaptive.org",
              636,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
            new LdapURL.Entry(
              "ldap",
              "directory2.ldaptive.org",
              10389,
              LdapURL.Entry.DEFAULT_BASE_DN,
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org/dc=ldaptive,dc=org " +
              "ldap://directory2.ldaptive.org:10389/dc=ldaptive,dc=org"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              "directory1.ldaptive.org",
              636,
              "dc=ldaptive,dc=org",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
            new LdapURL.Entry(
              "ldap",
              "directory2.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              LdapURL.Entry.DEFAULT_ATTRIBUTES,
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org/dc=ldaptive,dc=org?cn,sn " +
              "ldap://directory2.ldaptive.org:10389/dc=ldaptive,dc=org?cn"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              "directory1.ldaptive.org",
              636,
              "dc=ldaptive,dc=org",
              new String[] {"cn", "sn"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
            new LdapURL.Entry(
              "ldap",
              "directory2.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn"},
              LdapURL.Entry.DEFAULT_SCOPE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org/dc=ldaptive,dc=org" +
              "?cn,sn?base ldap://directory2.ldaptive.org:10389/" +
              "dc=ldaptive,dc=org?cn?sub"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              "directory1.ldaptive.org",
              636,
              "dc=ldaptive,dc=org",
              new String[] {"cn", "sn"},
              SearchScope.OBJECT,
              LdapURL.Entry.DEFAULT_FILTER),
            new LdapURL.Entry(
              "ldap",
              "directory2.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn"},
              SearchScope.SUBTREE,
              LdapURL.Entry.DEFAULT_FILTER),
          },
        },
        new Object[] {
          new LdapURL(
            "ldaps://directory1.ldaptive.org/dc=ldaptive,dc=org?cn,sn?base?" +
              "(uid=dfisher) ldap://directory2.ldaptive.org:10389/" +
              "dc=ldaptive,dc=org?cn?sub?(uid=dfisher)"),
          new LdapURL.Entry[] {
            new LdapURL.Entry(
              "ldaps",
              "directory1.ldaptive.org",
              636,
              "dc=ldaptive,dc=org",
              new String[] {"cn", "sn"},
              SearchScope.OBJECT,
              new SearchFilter("(uid=dfisher)")),
            new LdapURL.Entry(
              "ldap",
              "directory2.ldaptive.org",
              10389,
              "dc=ldaptive,dc=org",
              new String[] {"cn"},
              SearchScope.SUBTREE,
              new SearchFilter("(uid=dfisher)")),
          },
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * @param  url  LdapUrl to test
   * @param  entries  to verify
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"ldapURL"},
    dataProvider = "urls"
  )
  public void testParsing(
    final LdapURL url,
    final LdapURL.Entry[] entries)
    throws Exception
  {
    final Iterator<LdapURL.Entry> iter = url.getEntries().iterator();
    Assert.assertEquals(entries.length, url.size());
    for (LdapURL.Entry entry : entries) {
      final LdapURL.Entry e = iter.next();
      compareEntries(entry, e);
    }

    compareEntries(entries[0], url.getEntry());
    compareEntries(entries[entries.length - 1], url.getLastEntry());

    final String[] hostnames = new String[entries.length];
    for (int i = 0; i < entries.length; i++) {
      hostnames[i] = entries[i].getHostname();
    }
    Assert.assertEquals(hostnames, url.getHostnames());

    final String[] hostnamesWithSchemeAndPort = new String[entries.length];
    for (int i = 0; i < entries.length; i++) {
      hostnamesWithSchemeAndPort[i] =
        entries[i].getHostnameWithSchemeAndPort();
    }
    Assert.assertEquals(
      hostnamesWithSchemeAndPort,
      url.getHostnamesWithSchemeAndPort());

    final String[] urls = new String[entries.length];
    for (int i = 0; i < entries.length; i++) {
      urls[i] = entries[i].getUrl();
    }
    Assert.assertEquals(urls, url.getUrls());
  }


  /**
   * Compare all the properties of the supplied entries.
   *
   * @param  entry1  to compare
   * @param  entry2  to compare
   */
  private void compareEntries(
    final LdapURL.Entry entry1,
    final LdapURL.Entry entry2)
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
