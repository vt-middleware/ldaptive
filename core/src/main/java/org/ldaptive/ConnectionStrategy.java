/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
   * Returns the period at which inactive connections will be tested.
   *
   * @return  inactive period
   */
  Duration getInactivePeriod();


  /**
   * Returns the condition under which an inactive test should be executed.
   *
   * @return  inactive condition
   */
  Predicate<RetryMetadata> getInactiveCondition();


  /**
   * Returns an ordered list of LDAP URLs to attempt connections to.
   *
   * @return  ordered LDAP URLs
   */
  List<LdapURL> apply();


  /**
   * Returns all active LDAP URLs.
   *
   * @return  active URLs
   */
  Map<Integer, LdapURL> active();


  /**
   * Returns all inactive LDAP URLs.
   *
   * @return  inactive URLs
   */
  Map<RetryMetadata, Map.Entry<Integer, LdapURL>> inactive();


  /**
   * Indicates the supplied URL was successfully connected to.
   *
   * @param  url  which was successfully connected to
   */
  void success(LdapURL url);


  /**
   * Indicates the supplied URL could not be connected to.
   *
   * @param  url  which was could not be connected to
   */
  void failure(LdapURL url);
}
