/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LdapURLSet}.
 *
 * @author  Middleware Services
 */
public class LdapURLSetTest
{


  /**
   * URL test data.
   *
   * @return  test data
   */
  @DataProvider(name = "urls")
  public Object[][] createURLs()
  {
    return
      new Object[][] {
        // active passive
        new Object[] {
          new LdapURLSet(
            new ActivePassiveConnectionStrategy(),
            "ldap://directory.ldaptive.org"),
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          new LdapURLSet(
            new ActivePassiveConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          new LdapURLSet(
            new ActivePassiveConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
        // random
        new Object[] {
          new LdapURLSet(
            new RandomConnectionStrategy(),
            "ldap://directory.ldaptive.org"),
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          new LdapURLSet(
            new RandomConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          new LdapURLSet(
            new RandomConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
        // round robin
        new Object[] {
          new LdapURLSet(
            new RoundRobinConnectionStrategy(),
            "ldap://directory.ldaptive.org"),
          new LdapURL[] {new LdapURL("ldap://directory.ldaptive.org")},
        },
        new Object[] {
          new LdapURLSet(
            new RoundRobinConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
          },
        },
        new Object[] {
          new LdapURLSet(
            new RoundRobinConnectionStrategy(),
            "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org"),
          new LdapURL[] {
            new LdapURL("ldap://directory-1.ldaptive.org"),
            new LdapURL("ldap://directory-2.ldaptive.org"),
            new LdapURL("ldap://directory-3.ldaptive.org"),
          },
        },
      };
  }


  @Test(groups = "conn", dataProvider = "urls")
  public void populate(final LdapURLSet urlSet, final LdapURL[] expected)
    throws Exception
  {
    assertThat(urlSet.hasActiveUrls()).isTrue();
    assertThat(urlSet.getActiveUrls()).hasSize(expected.length);
    assertThat(urlSet.hasInactiveUrls()).isFalse();
    assertThat(urlSet.getInactiveUrls()).isEmpty();
    for (int i = 0; i < expected.length; i++) {
      assertThat(urlSet.getActiveUrls().get(i)).isEqualTo(expected[i]);
    }
  }


  @Test(groups = "conn", dataProvider = "urls")
  public void deactivate(final LdapURLSet urlSet, final LdapURL[] expected)
    throws Exception
  {
    assertThat(urlSet.hasActiveUrls()).isTrue();
    assertThat(urlSet.getActiveUrls().size()).isEqualTo(expected.length);
    assertThat(urlSet.hasInactiveUrls()).isFalse();
    assertThat(urlSet.getInactiveUrls()).isEmpty();

    // deactivate URLs
    while (urlSet.hasActiveUrls()) {
      urlSet.getActiveUrls().get(0).deactivate();
    }
    assertThat(urlSet.hasActiveUrls()).isFalse();
    assertThat(urlSet.getActiveUrls()).isEmpty();
    assertThat(urlSet.hasInactiveUrls()).isTrue();
    assertThat(urlSet.getInactiveUrls().size()).isEqualTo(expected.length);

    // activate URLs
    while (urlSet.hasInactiveUrls()) {
      urlSet.getInactiveUrls().get(0).activate();
    }
    assertThat(urlSet.hasActiveUrls()).isTrue();
    assertThat(urlSet.getActiveUrls().size()).isEqualTo(expected.length);
    assertThat(urlSet.hasInactiveUrls()).isFalse();
    assertThat(urlSet.getInactiveUrls()).isEmpty();
  }
}
