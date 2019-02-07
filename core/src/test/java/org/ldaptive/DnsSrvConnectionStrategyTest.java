/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
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
   * Unit test for {@link DnsSrvConnectionStrategy#apply()}.
   */
  @Test
  public void applyDefault()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(
      contextFactory, DnsSrvConnectionStrategy.DEFAULT_TTL);
    strategy.initialize(null);
    final List<LdapURL> urls = strategy.apply();
    Assert.assertEquals(urls.size(), 3);
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#apply()}.
   */
  @Test
  public void applyMultiple()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy(
      contextFactory, DnsSrvConnectionStrategy.DEFAULT_TTL);
    strategy.initialize("dns:?_ldap._tcp.dne.ldaptive.org dns:");
    final List<LdapURL> urls = strategy.apply();
    Assert.assertEquals(urls.size(), 3);
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#apply()}.
   */
  @Test
  public void applyCustom()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy();
    strategy.initialize("dns:?_ldap._tcp.w2k.vt.edu");
    final List<LdapURL> urls = strategy.apply();
    Assert.assertEquals(urls.size(), 3);
  }


  /**
   * Unit test for {@link DnsSrvConnectionStrategy#apply()}.
   */
  @Test
  public void applyEmpty()
  {
    final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy();
    strategy.initialize(null);
  }
}
