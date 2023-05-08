/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

/**
 * Provides the default implementation of the JNDI context factory for DNS queries.
 *
 * @author  Middleware Services
 */
public class DefaultDNSContextFactory implements DNSContextFactory
{

  /** JNDI context factory for DNS. */
  public static final String DNS_CONTEXT_FACTORY = "com.sun.jndi.dns.DnsContextFactory";

  /** Default provider URL for DNS, determines DNS from the underlying OS. Value is {@value}. */
  public static final String DEFAULT_DNS_PROVIDER_URL = "dns:";

  /** DNS name servers in order of preference. */
  private final List<String> nameservers;


  /**
   * Creates a new instance that resolves DNS names using the given name servers.
   *
   * @param  servers  name servers in order of preference.
   */
  public DefaultDNSContextFactory(final String... servers)
  {
    if (servers != null && servers.length > 0) {
      nameservers = Arrays.asList(servers);
    } else {
      nameservers = Collections.emptyList();
    }
  }


  @Override
  public InitialDirContext create()
    throws NamingException
  {
    final Map<String, Object> env = new HashMap<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, DNS_CONTEXT_FACTORY);
    if (nameservers.isEmpty()) {
      env.put(Context.PROVIDER_URL, DEFAULT_DNS_PROVIDER_URL);
    } else {
      env.put(
        Context.PROVIDER_URL,
        String.join(" ", nameservers));
    }
    // CheckStyle:IllegalType OFF
    return new InitialDirContext(new Hashtable<>(env));
    // CheckStyle:IllegalType ON
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "nameservers=" + nameservers;
  }
}
