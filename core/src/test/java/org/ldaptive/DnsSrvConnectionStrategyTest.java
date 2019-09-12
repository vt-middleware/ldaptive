/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.ldaptive.dns.DNSContextFactory;
import org.ldaptive.dns.MockDirContext;
import org.ldaptive.provider.mock.MockConnection;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DnsSrvConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class DnsSrvConnectionStrategyTest
{

  /** Mock resolver. */
  private DNSContextFactory contextFactory;


  /**
   * Initialize the context factory.
   *
   * @throws  Exception  on test failure
   */
  @BeforeTest
  public void setUp()
    throws Exception
  {
    final MockDirContext context = new MockDirContext();
    context.addAttribute(
      "_ldap._tcp.ldaptive.org",
      "SRV",
      "1 0 389 directory-1.ldaptive.org",
      "3 200 389 directory-2.ldaptive.org",
      "5 100 389 directory-3.ldaptive.org");
    context.addAttribute(
      "_ldap._tcp",
      "SRV",
      "1 0 389 directory-1.ldaptive.org",
      "3 200 389 directory-2.ldaptive.org",
      "5 100 389 directory-3.ldaptive.org");
    contextFactory = () -> context;
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#parseDnsUrl(String)}.
   */
  @Test
  public void parseDnsUrl()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy();
    Assert.assertEquals(strategy.parseDnsUrl("dns:"), new String[] {"dns:", null});
    Assert.assertEquals(
      strategy.parseDnsUrl("dns:?_ldap._tcp.ldaptive.org"),
      new String[] {"dns:", "_ldap._tcp.ldaptive.org"});
    Assert.assertEquals(strategy.parseDnsUrl("dns://dns.server.com"), new String[] {"dns://dns.server.com", null});
    Assert.assertEquals(
      strategy.parseDnsUrl("dns://dns.server.com/ldaptive.org"),
      new String[] {"dns://dns.server.com/ldaptive.org", null});
    Assert.assertEquals(
      strategy.parseDnsUrl("dns://dns.server.com/ldaptive.org?_ldap._tcp"),
      new String[] {"dns://dns.server.com/ldaptive.org", "_ldap._tcp"});
    Assert.assertEquals(
      strategy.parseDnsUrl("dns://dns.server.com?_ldap._tcp"),
      new String[] {"dns://dns.server.com", "_ldap._tcp"});
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#iterator()}.
   */
  @Test
  public void iteratorDefault()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(contextFactory);
    strategy.initialize(null, ldapURL -> true);
    final List<LdapURL> urls = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(urls.size(), 3);
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#iterator()}.
   */
  @Test
  public void iteratorMultiple()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(
      contextFactory, DnsSrvConnectionStrategy.DEFAULT_TTL);
    strategy.initialize("dns:?_ldap._tcp.dne.ldaptive.org dns:", ldapURL -> true);
    final List<LdapURL> urls = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(urls.size(), 3);
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#iterator()}.
   */
  @Test
  public void iteratorCustom()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy();
    strategy.initialize("dns:?_ldap._tcp.w2k.vt.edu", ldapURL -> true);
    final List<LdapURL> urls = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(urls.size(), 3);
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#iterator()}.
   */
  @Test
  public void iteratorEmpty()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy();
    strategy.initialize(null, ldapURL -> true);
    final List<LdapURL> urls = StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList());
    Assert.assertEquals(urls.size(), 0);
  }


  @Test(groups = "conn")
  public void firstUrlInactive()
    throws Exception
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(contextFactory);
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("dns://");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(ldapURL -> !ldapURL.getHostname().contains("-1"));
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first entry should fail, list should reorder with that entry last
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-1.ldaptive.org:389"));

    // confirm the inactive entry stays at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389"),
        new LdapURL("ldap://directory-1.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-1.ldaptive.org:389"));

    // mark first entry as active, list should reorder with that entry first
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
  }


  @Test(groups = "conn")
  public void firstAndSecondUrlInactive()
    throws Exception
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(contextFactory);
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl("dns://");
    cc.setConnectionStrategy(strategy);
    final MockConnection conn = new MockConnection(cc);
    conn.setOpenPredicate(ldapURL -> !ldapURL.getHostname().contains("-1") && !ldapURL.getHostname().contains("-2"));
    conn.setTestPredicate(ldapURL -> true);
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);

    // first and second entry should fail, list should reorder with those entries last
    conn.open();
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls().iterator().next(),
      new LdapURL("ldap://directory-3.ldaptive.org:389"));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389")));

    // confirm the inactive entries stay at the end
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-3.ldaptive.org:389"),
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls().iterator().next(),
      new LdapURL("ldap://directory-3.ldaptive.org:389"));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389")));

    // mark first entry as active, list should reorder with that entry first
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 2);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 1);
    Assert.assertEquals(
      strategy.ldapURLSet.getInactiveUrls().iterator().next(),
      new LdapURL("ldap://directory-2.ldaptive.org:389"));

    // mark second entry as active, list should reorder with that entry second
    strategy.success(strategy.ldapURLSet.getInactiveUrls().iterator().next());
    Assert.assertEquals(
      StreamSupport.stream(strategy.spliterator(), false).collect(Collectors.toList()),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getActiveUrls().size(), 3);
    Assert.assertEquals(
      strategy.ldapURLSet.getActiveUrls(),
      List.of(
        new LdapURL("ldap://directory-1.ldaptive.org:389"),
        new LdapURL("ldap://directory-2.ldaptive.org:389"),
        new LdapURL("ldap://directory-3.ldaptive.org:389")));
    Assert.assertEquals(strategy.ldapURLSet.getInactiveUrls().size(), 0);
  }
}
