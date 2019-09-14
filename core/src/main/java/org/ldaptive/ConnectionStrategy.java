/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.function.Predicate;

/**
 * Interface to describe various connection strategies. Each strategy returns an ordered list of LDAP URLs to attempt
 * when opening a connection.
 *
 * @author  Middleware Services
 */
public interface ConnectionStrategy extends Iterable<LdapURL>
{


  /**
   * Populates a {@link LdapURLSet} from the URL string provided at configuration time.
   *
   * @param  urls  Space-delimited string of URLs describing the LDAP hosts to connect to. The URLs in the string
   *               are commonly {@code ldap://} or {@code ldaps://} URLs that directly describe the hosts to connect to,
   *               but may also describe a resource from which to obtain LDAP connection URLs as is the case for
   *               {@link DnsSrvConnectionStrategy} that use URLs with the scheme {@code dns:}.
   * @param  urlSet  LDAP URL set to populate.
   */
  void populate(String urls, LdapURLSet urlSet);


  /**
   * Prepare this strategy for use.
   *
   * @param  urls  LDAP URLs for this strategy
   * @param  activateCondition  predicate to determine whether a connection is active
   */
  void initialize(String urls, Predicate<LdapURL> activateCondition);


  /**
   * Whether this strategy is ready for use.
   *
   * @return  whether this strategy is ready for use
   */
  boolean isInitialized();


  /**
   * Returns the condition used to activate connections.
   *
   * @return  activate condition
   */
  Predicate<LdapURL> getActivateCondition();


  /**
   * Returns the condition used to determine whether to attempt to activate a connection.
   *
   * @return  retry condition
   */
  Predicate<LdapURL> getRetryCondition();


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
