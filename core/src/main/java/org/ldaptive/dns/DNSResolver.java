/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dns;

import java.util.Set;

/**
 * Strategy pattern interface for resolving DNS records.
 *
 * @param <T> Type of record to resolve.
 *
 * @author  Middleware Services
 */
public interface DNSResolver<T>
{


  /**
   * Resolve a set of DNS records of some type for the given name.
   *
   * @param  name  Name for which to resolve DNS records.
   *
   * @return Set of records of type T bound to the given name.
   */
  Set<T> resolve(String name);
}
