/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DNS SRV connection strategy. Queries a DNS server for SRV records and uses those records to construct a list of URLs.
 * A time to live can be set to control how often the DNS server is consulted. See http://www.ietf.org/rfc/rfc2782.txt.
 *
 * @author  Middleware Services
 */
public class DnsSrvConnectionStrategy implements ConnectionStrategy
{

  /** JNDI context factory for DNS. */
  private static final String DNS_CONTEXT_FACTORY = "com.sun.jndi.dns.DnsContextFactory";

  /** JNDI context factory for DNS. */
  private static final String DNS_PROVIDER_URL = "dns:";

  /** Default time to live for DNS results. Value is {@value}. */
  private static final long DEFAULT_TTL = 60L * 60L * 1000L;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** JNDI environment. */
  private Map<String, Object> jndiEnv = new HashMap<>();

  /** Time to live for SRV records in milliseconds. */
  private long srvTtl;

  /** SRV records from the last DNS lookup. */
  private List<SrvRecord> srvRecords;


  /** Creates a new DNS SRV connection strategy. */
  public DnsSrvConnectionStrategy()
  {
    this(null, DEFAULT_TTL);
  }


  /**
   * Creates a new DNS SRV connection strategy.
   *
   * @param  ttl  time to live in milliseconds for SRV records
   */
  public DnsSrvConnectionStrategy(final long ttl)
  {
    this(null, ttl);
  }


  /**
   * Creates a new DNS SRV connection strategy.
   *
   * @param  env  JNDI environment
   * @param  ttl  time to live in milliseconds for SRV records
   */
  public DnsSrvConnectionStrategy(final Map<String, Object> env, final long ttl)
  {
    if (env != null) {
      setJndiEnvironment(env);
    }
    setTimeToLive(ttl);
  }


  /**
   * Returns the JNDI environment used for DNS lookup.
   *
   * @return  jndi environment
   */
  public Map<String, Object> getJndiEnvironment()
  {
    return jndiEnv;
  }


  /**
   * Returns the time that DNS lookups will be cached.
   *
   * @return  time to live in milliseconds
   */
  public long getTimeToLive()
  {
    return srvTtl;
  }


  /**
   * Sets the JNDI environment used for DNS lookups. If no {@link Context#INITIAL_CONTEXT_FACTORY} is set, it is
   * defaulted to {@link #DNS_CONTEXT_FACTORY}. If no {@link Context#PROVIDER_URL} is set, it is defaulted to {@link
   * #DNS_PROVIDER_URL}.
   *
   * @param  env  jndi environment or null
   */
  public void setJndiEnvironment(final Map<String, Object> env)
  {
    jndiEnv = new HashMap<>(env);
  }


  /**
   * Sets the time that DNS lookups will be cached.
   *
   * @param  ttl  time to live in milliseconds
   */
  public void setTimeToLive(final long ttl)
  {
    srvTtl = ttl;
  }


  /**
   * Returns a list of URLs retrieved from DNS SRV records. The LDAP URL in the supplied metadata can be a space
   * delimited list of DNS servers, each will be tried in order.
   *
   * @param  metadata  which can be used to produce the URL list
   *
   * @return  list of URLs to attempt connections to
   */
  @Override
  public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
  {
    if (metadata == null || metadata.getLdapUrl() == null) {
      return null;
    }
    if (srvRecords == null ||
        srvRecords.isEmpty() ||
        System.currentTimeMillis() >= srvRecords.get(0).getExpirationTime()) {
      try {
        srvRecords = sortSrvRecords(retrieveDNSRecords(metadata.getLdapUrl(), jndiEnv, srvTtl));
      } catch (NamingException e) {
        throw new IllegalArgumentException("Could not retrieve DNS SRV record for " + metadata.getLdapUrl(), e);
      }
      if (srvRecords.isEmpty()) {
        throw new IllegalArgumentException("No DNS SRV records found for " + metadata.getLdapUrl());
      }
      logger.debug("Retrieved SRV records from DNS: {}", srvRecords);
    } else {
      logger.debug("Using SRV records from internal cache: {}", srvRecords);
    }

    final String[] urls = new String[srvRecords.size()];
    for (int i = 0; i < srvRecords.size(); i++) {
      urls[i] = srvRecords.get(i).getLdapURL();
    }
    return urls;
  }


  /**
   * Uses JNDI to retrieve the DNS SRV record from the supplied url. The supplied properties are passed into the JNDI
   * context.
   *
   * @param  name  of the SRV records
   * @param  props  for the JNDI context
   * @param  ttl  time to live for each SRV record
   *
   * @return  list of LDAP URLs
   *
   * @throws  NamingException  if the DNS record cannot be retrieved
   */
  protected List<SrvRecord> retrieveDNSRecords(final String name, final Map<String, Object> props, final long ttl)
    throws NamingException
  {
    final List<SrvRecord> records = new ArrayList<>();
    DirContext context = null;
    NamingEnumeration<?> en = null;
    try {
      // CheckStyle:IllegalType OFF
      final Hashtable<String, Object> env = new Hashtable<>(props);
      // CheckStyle:IllegalType ON
      if (!env.containsKey(Context.INITIAL_CONTEXT_FACTORY)) {
        env.put(Context.INITIAL_CONTEXT_FACTORY, DNS_CONTEXT_FACTORY);
      }
      if (!env.containsKey(Context.PROVIDER_URL)) {
        env.put(Context.PROVIDER_URL, DNS_PROVIDER_URL);
      }
      context = new InitialDirContext(env);

      final Attributes attrs = context.getAttributes(name, new String[] {"SRV", });
      if (attrs != null) {
        final Attribute attr = attrs.get("SRV");
        if (attr != null) {
          en = attr.getAll();

          final long expTime = System.currentTimeMillis() + ttl;
          while (en.hasMore()) {
            records.add(new SrvRecord((String) en.next(), expTime));
          }
        }
      }
    } finally {
      if (en != null) {
        en.close();
      }
      if (context != null) {
        context.close();
      }
    }
    return records;
  }


  /**
   * Sorts the supplied SRV records according to RFC 2782. Records with the lowest priority are first. Records with the
   * same priority are arranged by weight with higher weights having a greater chance to be ordered first.
   *
   * @param  records  to sort
   *
   * @return  sorted records
   */
  protected List<SrvRecord> sortSrvRecords(final List<SrvRecord> records)
  {
    // group records and order them by priority
    final Map<Long, List<SrvRecord>> priorityRecords = new TreeMap<>();
    for (SrvRecord record : records) {
      final List<SrvRecord> priority;
      if (!priorityRecords.containsKey(record.getPriority())) {
        priority = new ArrayList<>();
        priorityRecords.put(record.getPriority(), priority);
      } else {
        priority = priorityRecords.get(record.getPriority());
      }
      priority.add(record);
    }

    // order records by priority then by weight
    // unweighted records are ordered last
    final List<SrvRecord> sortedRecords = new ArrayList<>();
    for (Map.Entry<Long, List<SrvRecord>> entry : priorityRecords.entrySet()) {
      final Map<Long, SrvRecord> weighted = new HashMap<>();
      final List<SrvRecord> unweighted = new ArrayList<>();
      long totalWeight = 0;
      for (SrvRecord record : entry.getValue()) {
        if (record.getWeight() == 0) {
          unweighted.add(record);
        } else {
          totalWeight += record.getWeight();
          weighted.put(totalWeight, record);
        }
      }

      while (!weighted.isEmpty()) {
        SrvRecord record = null;
        final Iterator<Long> i = weighted.keySet().iterator();
        final long random = ThreadLocalRandom.current().nextLong(totalWeight + 1);
        while (i.hasNext()) {
          final Long weight = i.next();
          if (weight >= random) {
            record = weighted.get(weight);
            totalWeight -= record.getWeight();
            i.remove();
            break;
          }
        }
        sortedRecords.add(record);
      }
      if (!unweighted.isEmpty()) {
        sortedRecords.addAll(unweighted);
      }
    }

    return sortedRecords;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::jndiEnv=%s, srvTtl=%s, srvRecords=%s]",
        getClass().getName(),
        hashCode(),
        jndiEnv,
        srvTtl,
        srvRecords);
  }


  /** SRV record. */
  protected static class SrvRecord
  {

    /** hash code seed. */
    private static final int HASH_CODE_SEED = 1201;

    /** SRV priority. */
    private final long priority;

    /** SRV weight. */
    private final long weight;

    /** SRV port. */
    private final int port;

    /** SRV target. */
    private final String target;

    /** expiration time. */
    private final long expirationTime;


    /**
     * Creates a new SRV record.
     *
     * @param  record  from DNS
     * @param  time  that this record should expire
     */
    public SrvRecord(final String record, final long time)
    {
      final String[] parts = record.split(" ");
      int i = 0;
      priority = Long.parseLong(parts[i++]);
      weight = Long.parseLong(parts[i++]);
      port = Integer.parseInt(parts[i++]);
      target = parts[i].endsWith(".") ? parts[i].substring(0, parts[i].length() - 1) : parts[i];
      expirationTime = time;
    }


    /**
     * Returns the priority.
     *
     * @return  priority
     */
    public long getPriority()
    {
      return priority;
    }


    /**
     * Returns the weight.
     *
     * @return  weight
     */
    public long getWeight()
    {
      return weight;
    }


    /**
     * Returns the port.
     *
     * @return  port
     */
    public int getPort()
    {
      return port;
    }


    /**
     * Returns the target.
     *
     * @return  target
     */
    public String getTarget()
    {
      return target;
    }


    /**
     * Returns the target properly formatted as an LDAP URL.
     *
     * @return  LDAP URL
     */
    public String getLdapURL()
    {
      return String.format("ldap://%s:%s", target, port);
    }


    /**
     * Returns the time in milliseconds that this record should expire.
     *
     * @return  expiration time
     */
    public long getExpirationTime()
    {
      return expirationTime;
    }


    @Override
    public boolean equals(final Object o)
    {
      return LdapUtils.areEqual(this, o);
    }


    @Override
    public int hashCode()
    {
      return LdapUtils.computeHashCode(HASH_CODE_SEED, priority, weight, port, target, expirationTime);
    }


    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::priority=%s, weight=%s, port=%s, target=%s, " +
          "expirationTime=%s]",
          getClass().getName(),
          hashCode(),
          priority,
          weight,
          port,
          target,
          expirationTime);
    }
  }
}
