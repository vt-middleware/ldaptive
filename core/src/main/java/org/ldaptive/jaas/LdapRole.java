/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResponse;

/**
 * Provides a custom implementation for adding LDAP principals to a subject that represent roles.
 *
 * @author  Middleware Services
 */
public class LdapRole implements Principal, Comparable<Principal>
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 421;

  /** LDAP role name. */
  private final String roleName;


  /**
   * Creates a new ldap role with the supplied name.
   *
   * @param  name  of this role
   */
  public LdapRole(final String name)
  {
    roleName = name;
  }


  @Override
  public String getName()
  {
    return roleName;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapRole) {
      final LdapRole v = (LdapRole) o;
      return LdapUtils.areEqual(roleName, v.roleName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, roleName);
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "roleName=" + roleName + "]";
  }


  @Override
  public int compareTo(final Principal p)
  {
    return roleName.compareTo(p.getName());
  }


  /**
   * Iterates over the supplied result and returns all attributes as a set of ldap roles.
   *
   * @param  result  to read
   *
   * @return  ldap roles
   */
  public static Set<LdapRole> toRoles(final SearchResponse result)
  {
    final Set<LdapRole> r = new HashSet<>();
    for (LdapEntry le : result.getEntries()) {
      r.addAll(toRoles(le));
    }
    return r;
  }


  /**
   * Iterates over the supplied entry and returns all attributes as a set of ldap roles.
   *
   * @param  entry  to read
   *
   * @return  ldap roles
   */
  public static Set<LdapRole> toRoles(final LdapEntry entry)
  {
    return toRoles(entry.getAttributes());
  }


  /**
   * Iterates over the supplied attributes and returns all values as a set of ldap roles.
   *
   * @param  attributes  to read
   *
   * @return  ldap roles
   */
  public static Set<LdapRole> toRoles(final Collection<LdapAttribute> attributes)
  {
    final Set<LdapRole> r = new HashSet<>();
    if (attributes != null) {
      for (LdapAttribute ldapAttr : attributes) {
        r.addAll(ldapAttr.getStringValues().stream().map(LdapRole::new).collect(Collectors.toList()));
      }
    }
    return r;
  }
}
