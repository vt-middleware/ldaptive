/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import org.ldaptive.LdapURL;
import org.ldaptive.LdapUtils;

/**
 * Class to contain the properties of a DNS SRV record.
 *
 * @author  Middleware Services
 */
public class SRVRecord
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

  /** whether to use LDAPS. */
  private final boolean useSSL;


  /**
   * Creates a new SRV record.
   *
   * @param  record  from DNS
   * @param  ssl  whether to use LDAPS
   */
  public SRVRecord(final String record, final boolean ssl)
  {
    final String[] parts = record.split(" ");
    int i = 0;
    priority = Long.parseLong(parts[i++]);
    weight = Long.parseLong(parts[i++]);
    port = Integer.parseInt(parts[i++]);
    target = parts[i].endsWith(".") ? parts[i].substring(0, parts[i].length() - 1) : parts[i];
    useSSL = ssl;
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
  public LdapURL getLdapURL()
  {
    if (useSSL) {
      return new LdapURL("ldaps://" + target + ":" +  port);
    }
    return new LdapURL("ldap://" + target + ":" +  port);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SRVRecord) {
      final SRVRecord v = (SRVRecord) o;
      return LdapUtils.areEqual(priority, v.priority) &&
        LdapUtils.areEqual(weight, v.weight) &&
        LdapUtils.areEqual(port, v.port) &&
        LdapUtils.areEqual(target, v.target) &&
        LdapUtils.areEqual(useSSL, v.useSSL);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, priority, weight, port, target, useSSL);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      "[").append(getClass().getName()).append("@").append(hashCode()).append("::")
      .append("priority=").append(priority).append(", ")
      .append("weight=").append(weight).append(", ")
      .append("port=").append(port).append(", ")
      .append("target=").append(target).append(", ")
      .append("useSSL=").append(useSSL).append("]").toString();
  }
}
