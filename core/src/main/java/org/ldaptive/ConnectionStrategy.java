/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
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
   * Populates a {@link LdapURLSet} from the URL string provided at configuration time.
   *
   * @param urls Space-delimited string of URLs describing the LDAP hosts to connect to. The URLs in the string
   *             are commonly {@code ldap://} or {@code ldaps://} URLs that directly describe the hosts to connect to,
   *             but may also describe a resource from which to obtain LDAP connection URLs as is the case for
   *             {@link DnsSrvConnectionStrategy} that use URLs with the scheme {@code dns:}.
   * @param urlSet LDAP URL set to populate.
   */
  void populate(String urls, LdapURLSet urlSet);


  /**
   * Produces the next active LDAP URL to use from the given set.
   *
   * @param urlSet Set of LDAP URLs.
   *
   * @return Next active LDAP URL.
   */
  LdapURL next(LdapURLSet urlSet);


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
  Predicate<LdapURL> getInactiveCondition();
}
