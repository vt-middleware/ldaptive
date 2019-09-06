/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of LDAP URLs with helper functions for common connection strategies.
 *
 * @author  Middleware Services
 */
public class LdapURLSet
{

  /** Type of backing map. */
  public enum Type {

    /** sorted list. */
    SORTED,

    /** ordered list. */
    ORDERED,

    /** unordered list. */
    UNORDERED,
  }

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Active LDAP URLs. */
  protected Map<Integer, LdapURL> active;

  /** Inactive LDAP URLs. */
  protected Map<RetryMetadata, Map.Entry<Integer, LdapURL>> inactive = new LinkedHashMap<>();

  /** Lock for active and inactive maps. */
  protected Object lock = new Object();


  /**
   * Creates a new LDAP URL set of the supplied type.
   *
   * @param  type  of LDAP URL set
   */
  public LdapURLSet(final Type type)
  {
    switch(type) {
    case SORTED:
      active = new TreeMap<>();
      break;
    case ORDERED:
      active = new LinkedHashMap<>();
      break;
    case UNORDERED:
      active = new HashMap<>();
      break;
    default:
      throw new IllegalStateException("Unknown set type: " + type);
    }
  }


  /**
   * Adds new LDAP URLs to this set.
   *
   * @param  urls  to add
   */
  public void add(final LdapURL... urls)
  {
    synchronized (lock) {
      int i = active.keySet().size() + 1;
      for (LdapURL url : urls) {
        if (active.values().stream().anyMatch(u -> u.equals(url))) {
          throw new IllegalArgumentException("Duplicate LDAP URL " + url + " found in URL set: " + active);
        }
        active.put(i++, url);
      }
    }
  }


  /**
   * Moves the first URL in the set to the last. This operation is only meaningful for an ordered set.
   */
  public void firstToLast()
  {
    synchronized (lock) {
      final Map.Entry<Integer, LdapURL> entry = active.entrySet().iterator().next();
      active.remove(entry.getKey());
      active.put(entry.getKey(), entry.getValue());
    }
  }


  /**
   * Returns the number of URLs in this set.
   *
   * @return  number of URLs in this set
   */
  public int size()
  {
    synchronized (lock) {
      return active.size() + inactive.size();
    }
  }


  /**
   * Removes all URLs from this set.
   */
  public void clear()
  {
    synchronized (lock) {
      active.clear();
      inactive.clear();
    }
  }


  /**
   * Moves an LDAP URL from an inactive state to an active state. No-op if the supplied URL is not inactive.
   *
   * @param  url  to activate
   */
  public void activate(final LdapURL url)
  {
    synchronized (lock) {
      final Optional<Map.Entry<Integer, LdapURL>> entry = getInactive(url);
      if (entry.isPresent()) {
        inactive.entrySet().removeIf(e -> e.getValue().equals(entry.get()));
        active.put(entry.get().getKey(), entry.get().getValue());
      }
    }
  }


  /**
   * Moves an LDAP URL from an active state to an inactive state. No-op if the supplied URL is not active.
   *
   * @param  url  to activate
   */
  public void inactivate(final LdapURL url)
  {
    synchronized (lock) {
      final Optional<Map.Entry<Integer, LdapURL>> entry = getActive(url);
      if (entry.isPresent()) {
        active.entrySet().removeIf(e -> e.equals(entry.get()));
        inactive.put(new RetryMetadata(), entry.get());
      }
    }
  }


  /**
   * Returns the LDAP URLs in the order specified by the type of set. If a function is supplied, it is executed before
   * the inactive URLs are added to the end of the list.
   *
   * @param  function  to apply to the active URLs
   *
   * @return  list of LDAP URLs
   */
  public List<LdapURL> getUrls(final Consumer<List<LdapURL>> function)
  {
    synchronized (lock) {
      final List<LdapURL> l = new ArrayList<>();
      l.addAll(active.values());
      if (function != null) {
        function.accept(l);
      }
      if (inactive.size() > 0) {
        l.addAll(inactive.values().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
      }
      return Collections.unmodifiableList(l);
    }
  }


  /**
   * Returns all the active URLs with the ordered index it was added at.
   *
   * @return  active LDAP URLs
   */
  public Map<Integer, LdapURL> getActiveUrls()
  {
    synchronized (lock) {
      return Collections.unmodifiableMap(active);
    }
  }


  /**
   * Returns all the inactive URLs with the {@link RetryMetadata} associated with that LDAP URL.
   *
   * @return  inactive LDAP URLs
   */
  public Map<RetryMetadata, Map.Entry<Integer, LdapURL>> getInactiveUrls()
  {
    synchronized (lock) {
      return Collections.unmodifiableMap(inactive);
    }
  }


  /**
   * Returns the active entry for the supplied url.
   *
   * @param  url  to find
   *
   * @return  active entry or null
   */
  private Optional<Map.Entry<Integer, LdapURL>> getActive(final LdapURL url)
  {
    return active.entrySet().stream()
      .filter(e -> url.equals(e.getValue())).findAny();
  }


  /**
   * Returns the inactive entry for the supplied url.
   *
   * @param  url  to find
   *
   * @return  inactive entry or null
   */
  private Optional<Map.Entry<Integer, LdapURL>> getInactive(final LdapURL url)
  {
    return inactive.entrySet().stream()
      .filter(e -> url.equals(e.getValue().getValue())).map(e -> e.getValue()).findAny();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[")
      .append(getClass().getName()).append("@").append(hashCode()).append("::")
      .append("type=").append(active.getClass().getSimpleName()).append(", ")
      .append("active=").append(active).append(", ")
      .append("inactive=").append(inactive).append("]").toString();
  }
}
