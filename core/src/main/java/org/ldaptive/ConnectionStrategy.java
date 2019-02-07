/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface to describe various connection strategies. Each strategy returns an ordered list of LDAP URLs to attempt
 * when opening a connection.
 *
 * @author  Middleware Services
 */
public interface ConnectionStrategy
{


  /**
   * Prepare this strategy for use.
   *
   * @param  urls  LDAP URLs for this strategy
   */
  void initialize(String urls);


  /**
   * Whether this strategy is ready for use.
   *
   * @return  whether this strategy is ready for use
   */
  boolean isInitialized();


  /**
   * Returns the metadata for the URLs from this strategy.
   *
   * @return  URL metadata
   */
  Map<LdapURL, Map<String, Object>> getMetadata();


  /**
   * Returns an ordered list of LDAP URLs to connect to.
   *
   * @return  ordered LDAP URLs
   */
  List<LdapURL> apply();


  /**
   * Records a success for the supplied URL in the strategy metadata.
   *
   * @param  url  which was successfully connected to
   */
  default void success(final LdapURL url)
  {
    final Map<String, Object> keyValues = getMetadata().computeIfAbsent(url, ldapURL -> new HashMap<>());
    keyValues.computeIfPresent("successCount", (k, v) -> {
      if (v == null) {
        return 1L;
      }
      return (long) v + 1;
    });
  }


  /**
   * Records a failure for the supplied URL in the strategy metadata.
   *
   * @param  url  which was could not be connected to
   */
  default void failure(final LdapURL url)
  {
    final Map<String, Object> keyValues = getMetadata().computeIfAbsent(url, ldapURL -> new HashMap<>());
    keyValues.computeIfPresent("failureCount", (k, v) -> {
      if (v == null) {
        return 1L;
      }
      return (long) v + 1;
    });
  }
}
