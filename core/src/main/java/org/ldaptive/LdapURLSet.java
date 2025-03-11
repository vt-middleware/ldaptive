/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A set of LDAP URLs with helper functions for common connection strategies.
 *
 * @author  Middleware Services
 */
public final class LdapURLSet
{

  /** List of LDAP URLs to connect to in the order provided by the connection strategy. */
  private final List<LdapURL> urls = new ArrayList<>();


  /**
   * Creates a new LDAP URL set.
   *
   * @param strategy Connection strategy.
   * @param ldapUrls Space-delimited string of URLs describing the LDAP hosts to connect to. The URLs in the string
   *                 are commonly {@code ldap://} or {@code ldaps://} URLs that directly describe the hosts to connect
   *                 to, but may also describe a resource from which to obtain LDAP connection URLs as is the case for
   *                 {@link DnsSrvConnectionStrategy} that use URLs with the scheme {@code dns:}.
   */
  public LdapURLSet(final ConnectionStrategy strategy, final String ldapUrls)
  {
    LdapUtils.assertNotNullArg(strategy, "Connection strategy cannot be null");
    strategy.populate(ldapUrls, this);
  }


  /**
   * Returns the URLs in this set with active URLs ordered before inactive.
   *
   * @return  URLs with active ordered before inactive
   */
  public List<LdapURL> getUrls()
  {
    final List<LdapURL> l = new ArrayList<>(getActiveUrls());
    if (hasInactiveUrls()) {
      l.addAll(getInactiveUrls());
    }
    return Collections.unmodifiableList(l);
  }


  /**
   * Returns whether this set has any active URLs.
   *
   * @return  whether there are any active LDAP URLs in the set, false otherwise.
   */
  public boolean hasActiveUrls()
  {
    return urls.stream().anyMatch(LdapURL::isActive);
  }


  /**
   * Returns the active URLs.
   *
   * @return  list of active URLs in order they were added.
   */
  public List<LdapURL> getActiveUrls()
  {
    return urls.stream().filter(LdapURL::isActive).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Returns whether this set has any inactive URLs.
   *
   * @return  whether there are any inactive LDAP URLs in the set, false otherwise.
   */
  public boolean hasInactiveUrls()
  {
    return urls.stream().anyMatch(u -> !u.isActive());
  }


  /**
   * Returns the inactive URLs.
   *
   * @return  list of inactive URLs in order they were added.
   */
  public List<LdapURL> getInactiveUrls()
  {
    return urls.stream().filter(u -> !u.isActive()).collect(Collectors.toUnmodifiableList());
  }


  /**
   * Returns the number of URLs in this set.
   *
   * @return  number of URLs in this set
   */
  public int size()
  {
    return urls.size();
  }


  /**
   * Populates this set with a list of URLs in the order produced by
   * {@link ConnectionStrategy#populate(String, LdapURLSet)}. This method MUST be called before the set is used, but
   * MAY be called subsequently periodically to refresh the set of LDAP URLs.
   *
   * @param  ldapUrls  LDAP URLs to add to this set.
   */
  synchronized void populate(final List<LdapURL> ldapUrls)
  {
    // Copy activity state from any URLs currently in the set that match new entries
    for (LdapURL url : urls) {
      final LdapURL match = ldapUrls.stream().filter(u -> u.equals(url)).findFirst().orElse(null);
      if (match != null && !url.isActive()) {
        match.deactivate();
      }
    }
    urls.clear();
    urls.addAll(ldapUrls);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "active=" + getActiveUrls() + ", " +
      "inactive=" + getInactiveUrls() + "]";
  }
}
