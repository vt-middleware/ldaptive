/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DnsResolverConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class DnsResolverConnectionStrategyTest
{

  /** Mock DNS data. */
  private final Map<String, InetAddress[]> resolverMap = new HashMap<>();

  /** Custom resolver. */
  private final Function<String, InetAddress[]> customResolver = name -> resolverMap.get(name);

  /** Test URL. */
  private LdapURL directory1v4;

  /** Test URL. */
  private LdapURL directory1v6;

  /** Test URL. */
  private LdapURL directory2v4;

  /** Test URL. */
  private LdapURL directory2v6;

  /** Test URL. */
  private LdapURL directory3v4;


  /**
   * Initialize test data.
   *
   * @throws  Exception  on test failure
   */
  @BeforeTest
  public void setUp()
    throws Exception
  {
    resolverMap.put(
      "directory-1.ldaptive.org",
      new InetAddress[] {
        InetAddress.getByAddress(
          "directory-1.ldaptive.org",
          new byte[] {10, 10, 5, 2}),
        InetAddress.getByAddress(
          "directory-1.ldaptive.org",
          new byte[] {38, 7, -76, 0, 0, -112, 104, 0, 32, 0, 0, 0, 0, 0, 0, 100}),
      });
    resolverMap.put(
      "directory-2.ldaptive.org",
      new InetAddress[] {
        InetAddress.getByAddress(
          "directory-2.ldaptive.org",
          new byte[] {11, 11, 4, 3}),
        InetAddress.getByAddress(
          "directory-2.ldaptive.org",
          new byte[] {40, 70, -126, 0, 0, -112, 104, 0, 36, 0, 0, 0, 0, 0, 0, 99}),
      });
    resolverMap.put(
      "directory-3.ldaptive.org",
      new InetAddress[] {
        InetAddress.getByAddress(
          "directory-3.ldaptive.org",
          new byte[] {18, 18, 9, 6}),
      });

    directory1v4 = new LdapURL("ldap://directory-1.ldaptive.org");
    directory1v4.setInetAddress(InetAddress.getByAddress("directory-1.ldaptive.org", new byte[] {10, 10, 5, 2}));

    directory1v6 = new LdapURL("ldap://directory-1.ldaptive.org");
    directory1v6.setInetAddress(
      InetAddress.getByAddress("directory-1.ldaptive.org",
        new byte[] {38, 7, -76, 0, 0, -112, 104, 0, 32, 0, 0, 0, 0, 0, 0, 100}));

    directory2v4 = new LdapURL("ldap://directory-2.ldaptive.org");
    directory2v4.setInetAddress(InetAddress.getByAddress("directory-2.ldaptive.org", new byte[] {11, 11, 4, 3}));

    directory2v6 = new LdapURL("ldap://directory-2.ldaptive.org");
    directory2v6.setInetAddress(
      InetAddress.getByAddress("directory-2.ldaptive.org",
        new byte[] {40, 70, -126, 0, 0, -112, 104, 0, 36, 0, 0, 0, 0, 0, 0, 99}));

    directory3v4 = new LdapURL("ldap://directory-3.ldaptive.org");
    directory3v4.setInetAddress(InetAddress.getByAddress("directory-3.ldaptive.org", new byte[] {18, 18, 9, 6}));
  }


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
        new Object[] {
          "ldap://directory-1.ldaptive.org",
          new LdapURL[] {
            directory1v4,
            directory1v6,
          },
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org",
          new LdapURL[] {
            directory1v4,
            directory1v6,
            directory2v4,
            directory2v6,
          },
        },
        new Object[] {
          "ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org",
          new LdapURL[] {
            directory1v4,
            directory1v6,
            directory2v4,
            directory2v6,
            directory3v4,
          },
        },
      };
  }


  /**
   * Unit test for {@link DnsResolverConnectionStrategy#iterator()}.
   *
   * @param  actual  to initialize strategy with
   * @param  expected  to compare
   */
  @Test(groups = "conn", dataProvider = "urls")
  public void iterator(final String actual, final LdapURL[] expected)
  {
    final DnsResolverConnectionStrategy strategy = new DnsResolverConnectionStrategy();
    strategy.setResolverFunction(customResolver);
    strategy.initialize(actual, ldapURL -> true);
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()), Arrays.asList(expected));
  }


  @Test(groups = "conn")
  public void firstUrlInactive()
    throws Exception
  {
    final DnsResolverConnectionStrategy strategy = new DnsResolverConnectionStrategy();
    strategy.setResolverFunction(customResolver);
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(ldapURL -> !Arrays.equals(ldapURL.getInetAddress().getAddress(), new byte[] {10, 10, 5, 2}));
    conn.setTestPredicate(ldapURL -> true);

    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 5);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v4, directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first v4 entry should fail, list should reorder with that entry last
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 4);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().iterator().next(), directory1v4);

    // confirm the inactive entry stays at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      Arrays.asList(directory1v6, directory2v4, directory2v6, directory3v4, directory1v4));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 4);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().iterator().next(), directory1v4);

    // mark first entry as active, list should reorder with that entry first
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      Arrays.asList(directory1v4, directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 5);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v4, directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
  }


  @Test(groups = "conn")
  public void firstAndSecondUrlInactive()
    throws Exception
  {
    final DnsResolverConnectionStrategy strategy = new DnsResolverConnectionStrategy();
    strategy.setResolverFunction(customResolver);
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org ldap://directory-3.ldaptive.org");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(
      ldapURL ->
        !Arrays.equals(
          ldapURL.getInetAddress().getAddress(),
          new byte[] {10, 10, 5, 2}) &&
        !Arrays.equals(
          ldapURL.getInetAddress().getAddress(),
          new byte[] {38, 7, -76, 0, 0, -112, 104, 0, 32, 0, 0, 0, 0, 0, 0, 100}));
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 5);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v4, directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first ipv4 and ipv6 entries should fail, list should reorder with those entries last
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls(), Arrays.asList(directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls(), Arrays.asList(directory1v4, directory1v6));

    // confirm the inactive entries stay at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      Arrays.asList(directory2v4, directory2v6, directory3v4, directory1v4, directory1v6));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls(), Arrays.asList(directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls(), Arrays.asList(directory1v4, directory1v6));

    // mark first entry as active, list should reorder with that entry first
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      Arrays.asList(directory1v4, directory2v4, directory2v6, directory3v4, directory1v6));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 4);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v4, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().iterator().next(), directory1v6);

    // mark second entry as active, list should reorder with that entry second
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      Arrays.asList(directory1v4, directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 5);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      Arrays.asList(directory1v4, directory1v6, directory2v4, directory2v6, directory3v4));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
  }
}
