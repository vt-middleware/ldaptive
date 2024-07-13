/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.Iterator;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;


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
    assertThat(records.size()).isEqualTo(3);
    for (SRVRecord record : records) {
      assertThat(record.getLdapURL().getHostnameWithSchemeAndPort())
        .isEqualTo("ldap://" + record.getTarget() + ":" + record.getPort());
      assertThat(record.getPort()).isEqualTo(389);
      assertThat(record.getPriority()).isEqualTo(0);
      assertThat(record.getTarget().endsWith(".ldaptive.org")).isTrue();
      assertThat(record.getWeight()).isEqualTo(100);
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
    assertThat(records.size()).isEqualTo(3);
    final Iterator<SRVRecord> i = records.iterator();
    int count = 0;
    while (i.hasNext()) {
      final SRVRecord record = i.next();
      if (count == 0) {
        assertThat(record.getTarget()).isEqualTo("curly.ldaptive.org");
        assertThat(record.getPriority()).isEqualTo(1);
        assertThat(record.getWeight()).isEqualTo(0);
      } else if (count == 1) {
        assertThat(record.getTarget()).isEqualTo("moe.ldaptive.org");
        assertThat(record.getPriority()).isEqualTo(3);
        assertThat(record.getWeight()).isEqualTo(200);
      } else if (count == 2) {
        assertThat(record.getTarget()).isEqualTo("larry.ldaptive.org");
        assertThat(record.getPriority()).isEqualTo(5);
        assertThat(record.getWeight()).isEqualTo(100);
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
    assertThat(records.size()).isEqualTo(3);
    for (SRVRecord record : records) {
      assertThat("ldap://" + record.getTarget() + ":" + record.getPort())
        .isEqualTo(record.getLdapURL().getHostnameWithSchemeAndPort());
      assertThat(record.getPort()).isEqualTo(389);
      assertThat(record.getPriority()).isEqualTo(0);
      assertThat(record.getTarget().endsWith(".ldaptive.org")).isTrue();
      assertThat(record.getWeight()).isEqualTo(100);
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
    assertThat(records.size()).isGreaterThan(0);
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
