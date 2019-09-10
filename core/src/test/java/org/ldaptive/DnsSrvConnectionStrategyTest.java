/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.dns.DNSContextFactory;
import org.ldaptive.dns.MockDirContext;
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
   * Test DNS strategy with default DNS configuration.
   *
   * @throws  LdapException  On LDAP errors.
   */
  @Test
  public void nextWithDefault() throws LdapException
  {
    final LdapURLSet urlSet = new LdapURLSet(new DnsSrvConnectionStrategy(contextFactory), null);
    Assert.assertEquals(3, urlSet.getActiveUrls().size());
    Assert.assertEquals("directory-1.ldaptive.org", urlSet.getActiveUrls().get(0).getHostname());
    Assert.assertEquals("directory-2.ldaptive.org", urlSet.getActiveUrls().get(1).getHostname());
    Assert.assertEquals("directory-3.ldaptive.org", urlSet.getActiveUrls().get(2).getHostname());
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
  }


  /**
   * Test DNS strategy with multiple DNS URLs.
   *
   * @throws  LdapException  On LDAP errors.
   */
  @Test
  public void nextWithMultiple() throws LdapException
  {
    final LdapURLSet urlSet = new LdapURLSet(
        new DnsSrvConnectionStrategy(contextFactory, DnsSrvConnectionStrategy.DEFAULT_TTL),
        "dns:?_ldap._tcp.dne.ldaptive.org dns:");
    Assert.assertEquals("directory-1.ldaptive.org", urlSet.getActiveUrls().get(0).getHostname());
    Assert.assertEquals("directory-2.ldaptive.org", urlSet.getActiveUrls().get(1).getHostname());
    Assert.assertEquals("directory-3.ldaptive.org", urlSet.getActiveUrls().get(2).getHostname());
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
    urlSet.doWithNextActiveUrl(u -> Assert.assertEquals(u.getHostname(), "directory-1.ldaptive.org"));
  }


  /**
   * Test DNS strategy with live DNS records.
   */
  @Test
  public void nextWithCustom()
  {
    final LdapURLSet urlSet = new LdapURLSet(
        new DnsSrvConnectionStrategy(),
        "dns:?_ldap._tcp.w2k.vt.edu");
    Assert.assertEquals(3, urlSet.getActiveUrls().size());
  }


  /**
   * Test DNS strategy with no configuration to produce empty record set.
   *
   * @throws  LdapException  On LDAP errors.
   */
  @Test(expectedExceptions = IllegalStateException.class)
  public void nextWithEmpty() throws LdapException
  {
    final LdapURLSet urlSet = new LdapURLSet(new DnsSrvConnectionStrategy(), null);
    urlSet.doWithNextActiveUrl(u -> {});
  }


//  @Test(groups = "conn")
//  public void firstUrlInactive()
//    throws Exception
//  {
//    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(contextFactory);
//    final ConnectionConfig cc = new ConnectionConfig();
//    cc.setLdapUrl("dns://");
//    cc.setConnectionStrategy(strategy);
//    final MockConnection conn = new MockConnection(cc);
//    conn.setOpenPredicate(ldapURL -> !ldapURL.getHostname().contains("-1"));
//    conn.setTestPredicate(ldapURL -> true);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//
//    // first entry should fail, list should reorder with that entry last
//    conn.open();
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 2);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 1);
//    Assert.assertEquals(
//      strategy.ldapURLSet.inactive.values().iterator().next().getValue(),
//      new LdapURL("ldap://directory-1.ldaptive.org:389"));
//
//    // confirm the inactive entry stays at the end
//    Assert.assertEquals(
//      strategy.apply(),
//      List.of(
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389"),
//        new LdapURL("ldap://directory-1.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 2);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 1);
//    Assert.assertEquals(
//      strategy.ldapURLSet.inactive.values().iterator().next().getValue(),
//      new LdapURL("ldap://directory-1.ldaptive.org:389"));
//
//    // mark first entry as active, list should reorder with that entry first
//    strategy.success(new LdapURL("ldap://directory-1.ldaptive.org:389"));
//    Assert.assertEquals(
//      strategy.apply(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//  }
//
//
//  @Test(groups = "conn")
//  public void firstAndSecondUrlInactive()
//    throws Exception
//  {
//    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(contextFactory);
//    final ConnectionConfig cc = new ConnectionConfig();
//    cc.setLdapUrl("dns://");
//    cc.setConnectionStrategy(strategy);
//    final MockConnection conn = new MockConnection(cc);
//    conn.setOpenPredicate(ldapURL -> !ldapURL.getHostname().contains("-1") && !ldapURL.getHostname().contains("-2"));
//    conn.setTestPredicate(ldapURL -> true);
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//
//    // first and second entry should fail, list should reorder with those entries last
//    conn.open();
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 1);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values().iterator().next(),
//      new LdapURL("ldap://directory-3.ldaptive.org:389"));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 2);
//    Assert.assertEquals(
//      strategy.ldapURLSet.inactive.values().stream().map(e -> e.getValue()).collect(Collectors.toList()),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389")));
//
//    // confirm the inactive entries stay at the end
//    Assert.assertEquals(
//      strategy.apply(),
//      List.of(
//        new LdapURL("ldap://directory-3.ldaptive.org:389"),
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 1);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values().iterator().next(),
//      new LdapURL("ldap://directory-3.ldaptive.org:389"));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 2);
//    Assert.assertEquals(
//      strategy.ldapURLSet.inactive.values().stream().map(e -> e.getValue()).collect(Collectors.toList()),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389")));
//
//    // mark first entry as active, list should reorder with that entry first
//    strategy.success(new LdapURL("ldap://directory-1.ldaptive.org:389"));
//    Assert.assertEquals(
//      strategy.apply(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 2);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 1);
//    Assert.assertEquals(
//      strategy.ldapURLSet.inactive.values().iterator().next().getValue(),
//      new LdapURL("ldap://directory-2.ldaptive.org:389"));
//
//    // mark second entry as active, list should reorder with that entry second
//    strategy.success(new LdapURL("ldap://directory-2.ldaptive.org:389"));
//    Assert.assertEquals(
//      strategy.apply(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.active.size(), 3);
//    Assert.assertEquals(
//      strategy.ldapURLSet.active.values(),
//      List.of(
//        new LdapURL("ldap://directory-1.ldaptive.org:389"),
//        new LdapURL("ldap://directory-2.ldaptive.org:389"),
//        new LdapURL("ldap://directory-3.ldaptive.org:389")));
//    Assert.assertEquals(strategy.ldapURLSet.inactive.size(), 0);
//  }
}
