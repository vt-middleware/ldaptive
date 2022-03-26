/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.Iterator;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;


/**
 * Unit test for {@link SRVDNSResolver}.
 *
 * @author  Middleware Services
 */
public class SRVDNSResolverTest
{

  /** DNS context factory. */
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
      "0 100 389 larry.ldaptive.org",
      "0 100 389 curly.ldaptive.org",
      "0 100 389 moe.ldaptive.org");
    context.addAttribute(
      "_ldap._tcp.sorted.ldaptive.org",
      "SRV",
      "5 100 389 larry.ldaptive.org",
      "1 0 389 curly.ldaptive.org",
      "3 200 389 moe.ldaptive.org");
    context.addAttribute(
      "_ldap._tcp",
      "SRV",
      "0 100 389 directory-1.ldaptive.org",
      "0 100 389 directory-2.ldaptive.org",
      "0 100 389 directory-3.ldaptive.org");
    contextFactory = () -> context;
  }


  /**
   * Unit test for {@link SRVDNSResolver#resolve(String)}.
   */
  @Test
  public void resolve()
  {
    final SRVDNSResolver resolver = new SRVDNSResolver(contextFactory);
    final Set<SRVRecord> records = resolver.resolve("_ldap._tcp.ldaptive.org");
    Assert.assertEquals(records.size(), 3);
    for (SRVRecord record : records) {
      Assert.assertEquals(
        record.getLdapURL().getHostnameWithSchemeAndPort(),
        "ldap://" + record.getTarget() + ":" + record.getPort());
      Assert.assertEquals(record.getPort(), 389);
      Assert.assertEquals(record.getPriority(), 0);
      Assert.assertTrue(record.getTarget().endsWith(".ldaptive.org"));
      Assert.assertEquals(record.getWeight(), 100);
    }
  }


  /**
   * Unit test for {@link SRVDNSResolver#resolve(String)}.
   */
  @Test
  public void resolveSorted()
  {
    final SRVDNSResolver resolver = new SRVDNSResolver(contextFactory);
    final Set<SRVRecord> records = resolver.resolve("_ldap._tcp.sorted.ldaptive.org");
    Assert.assertEquals(records.size(), 3);
    final Iterator<SRVRecord> i = records.iterator();
    int count = 0;
    while (i.hasNext()) {
      final SRVRecord record = i.next();
      if (count == 0) {
        Assert.assertEquals(record.getTarget(), "curly.ldaptive.org");
        Assert.assertEquals(record.getPriority(), 1);
        Assert.assertEquals(record.getWeight(), 0);
      } else if (count == 1) {
        Assert.assertEquals(record.getTarget(), "moe.ldaptive.org");
        Assert.assertEquals(record.getPriority(), 3);
        Assert.assertEquals(record.getWeight(), 200);
      } else if (count == 2) {
        Assert.assertEquals(record.getTarget(), "larry.ldaptive.org");
        Assert.assertEquals(record.getPriority(), 5);
        Assert.assertEquals(record.getWeight(), 100);
      } else {
        throw new IllegalStateException("Unknown index:" + count);
      }
      count++;
    }
  }


  /**
   * Unit test for {@link SRVDNSResolver#resolve(String)}.
   */
  @Test
  public void resolveDefault()
  {
    final SRVDNSResolver resolver = new SRVDNSResolver(contextFactory);
    final Set<SRVRecord> records = resolver.resolve(null);
    Assert.assertEquals(records.size(), 3);
    for (SRVRecord record : records) {
      Assert.assertEquals(
        record.getLdapURL().getHostnameWithSchemeAndPort(),
        "ldap://" + record.getTarget() + ":" + record.getPort());
      Assert.assertEquals(record.getPort(), 389);
      Assert.assertEquals(record.getPriority(), 0);
      Assert.assertTrue(record.getTarget().endsWith(".ldaptive.org"));
      Assert.assertEquals(record.getWeight(), 100);
    }
  }


  /**
   * Unit test for {@link SRVDNSResolver#resolve(String)}. Set the following system properties:
   * SRVDNSResolverTest.nameserver=dns-server
   * SRVDNSResolverTest.name=host-name
   */
  @Test @Ignore
  public void resolveCustom()
  {
    final String nameserver = System.getProperty("SRVDNSResolverTest.nameserver");
    final SRVDNSResolver resolver;
    if (nameserver == null) {
      resolver = new SRVDNSResolver(new DefaultDNSContextFactory());
    } else {
      resolver = new SRVDNSResolver(new DefaultDNSContextFactory(nameserver));
    }
    final String recordName = System.getProperty("SRVDNSResolverTest.name");
    final Set<SRVRecord> records = resolver.resolve(recordName);
    Assert.assertTrue(records.size() > 0);
  }


  /**
   * Unit test for {@link SRVRecord#equals(Object)}.
   */
  @Test
  public void srvRecord()
  {
    EqualsVerifier.forClass(SRVRecord.class)
      .suppress(Warning.STRICT_INHERITANCE)
      .verify();
  }
}
