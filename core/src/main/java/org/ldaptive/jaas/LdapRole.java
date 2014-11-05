/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchResult;

/**
 * Provides a custom implementation for adding LDAP principals to a subject that
 * represent roles.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class LdapRole implements Principal, Serializable, Comparable<Principal>
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 421;

  /** serial version uid. */
  private static final long serialVersionUID = 1578734888816839199L;

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


  /** {@inheritDoc} */
  @Override
  public String getName()
  {
    return roleName;
  }


  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, roleName);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format("[%s@%d::%s]", getClass().getName(), hashCode(), roleName);
  }


  /** {@inheritDoc} */
  @Override
  public int compareTo(final Principal p)
  {
    return roleName.compareTo(p.getName());
  }


  /**
   * Iterates over the supplied result and returns all attributes as a set of
   * ldap roles.
   *
   * @param  result  to read
   *
   * @return  ldap roles
   */
  public static Set<LdapRole> toRoles(final SearchResult result)
  {
    final Set<LdapRole> r = new HashSet<>();
    for (LdapEntry le : result.getEntries()) {
      r.addAll(toRoles(le));
    }
    return r;
  }


  /**
   * Iterates over the supplied entry and returns all attributes as a set of
   * ldap roles.
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
   * Iterates over the supplied attributes and returns all values as a set of
   * ldap roles.
   *
   * @param  attributes  to read
   *
   * @return  ldap roles
   */
  public static Set<LdapRole> toRoles(
    final Collection<LdapAttribute> attributes)
  {
    final Set<LdapRole> r = new HashSet<>();
    if (attributes != null) {
      for (LdapAttribute ldapAttr : attributes) {
        for (String attrValue : ldapAttr.getStringValues()) {
          r.add(new LdapRole(attrValue));
        }
      }
    }
    return r;
  }
}
