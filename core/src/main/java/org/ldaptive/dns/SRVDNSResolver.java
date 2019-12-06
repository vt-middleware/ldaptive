/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Queries for DNS A records for a given host name.
 *
 * @author  Middleware Services
 */
public class SRVDNSResolver extends AbstractDNSResolver<SRVRecord>
{

  /** Attributes (DNS record types) to query for. */
  private static final String[] ATTRIBUTES = {"SRV", };

  /** Default DNS record name. */
  private static final String DEFAULT_RECORD_NAME = "_ldap._tcp";

  /** Connect to LDAP using LDAPS. */
  private boolean useSSL;


  /** Default constructor. */
  public SRVDNSResolver()
  {
    this(new DefaultDNSContextFactory());
  }


  /**
   * Creates a new DNS address resolver.
   *
   * @param  factory  JNDI dir context factory
   */
  public SRVDNSResolver(final DNSContextFactory factory)
  {
    this(factory, false);
  }


  /**
   * Creates a new DNS address resolver.
   *
   * @param  factory  JNDI dir context factory
   * @param  ssl  whether SRV records should produce LDAPS URLs
   */
  public SRVDNSResolver(final DNSContextFactory factory, final boolean ssl)
  {
    super(factory);
    useSSL = ssl;
  }


  @Override
  public Set<SRVRecord> resolve(final String name)
  {
    if (name == null) {
      return super.resolve(DEFAULT_RECORD_NAME);
    }
    return super.resolve(name);
  }


  @Override
  protected String[] getAttributes()
  {
    return ATTRIBUTES;
  }


  @Override
  protected Set<SRVRecord> processRecords(final Set<String> records)
  {
    final Set<SRVRecord> srvRecords = new HashSet<>(records.size());
    for (String record : records) {
      srvRecords.add(new SRVRecord(record, useSSL));
    }
    return sortSrvRecords(srvRecords);
  }


  /**
   * Sorts the supplied SRV records according to RFC 2782. Records with the lowest priority are first. Records with the
   * same priority are arranged by weight with higher weights having a greater chance to be ordered first.
   *
   * @param  records  to sort
   *
   * @return  sorted records
   */
  protected Set<SRVRecord> sortSrvRecords(final Set<SRVRecord> records)
  {
    // group records and order them by priority
    final Map<Long, Set<SRVRecord>> priorityRecords = new TreeMap<>();
    for (SRVRecord record : records) {
      final Set<SRVRecord> priority;
      if (!priorityRecords.containsKey(record.getPriority())) {
        priority = new LinkedHashSet<>();
        priorityRecords.put(record.getPriority(), priority);
      } else {
        priority = priorityRecords.get(record.getPriority());
      }
      priority.add(record);
    }

    // order records by priority then by weight
    // unweighted records are ordered last
    final Set<SRVRecord> sortedRecords = new LinkedHashSet<>();
    for (Map.Entry<Long, Set<SRVRecord>> entry : priorityRecords.entrySet()) {
      final Map<Long, SRVRecord> weighted = new HashMap<>();
      final Set<SRVRecord> unweighted = new LinkedHashSet<>();
      long totalWeight = 0;
      for (SRVRecord record : entry.getValue()) {
        if (record.getWeight() == 0) {
          unweighted.add(record);
        } else {
          totalWeight += record.getWeight();
          weighted.put(totalWeight, record);
        }
      }

      while (!weighted.isEmpty()) {
        SRVRecord record = null;
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
    return new StringBuilder("[").append(super.toString()).append(", ")
      .append("useSSL=").append(useSSL).append("]").toString();
  }
}
