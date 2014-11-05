/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.ldaptive.LdapUtils;

/**
 * Provides a custom implementation for grouping principals.
 *
 * @author  Middleware Services
 */
public class LdapGroup implements Group, Serializable
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 431;

  /** serial version uid. */
  private static final long serialVersionUID = 2075424472884533862L;

  /** LDAP role name. */
  private final String roleName;

  /** Principal members. */
  private final Set<Principal> members = new HashSet<>();


  /**
   * Creates a new ldap group with the supplied name.
   *
   * @param  name  of the group
   */
  public LdapGroup(final String name)
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
  public boolean addMember(final Principal user)
  {
    return members.add(user);
  }


  /** {@inheritDoc} */
  @Override
  public boolean removeMember(final Principal user)
  {
    return members.remove(user);
  }


  /** {@inheritDoc} */
  @Override
  public boolean isMember(final Principal member)
  {
    for (Principal p : members) {
      if (p.getName() != null && p.getName().equals(member.getName())) {
        return true;
      }
    }
    return false;
  }


  /** {@inheritDoc} */
  @Override
  public Enumeration<? extends Principal> members()
  {
    return Collections.enumeration(members);
  }


  /**
   * Returns an unmodifiable set of the members in this group.
   *
   * @return  set of member principals
   */
  public Set<Principal> getMembers()
  {
    return Collections.unmodifiableSet(members);
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
    return LdapUtils.computeHashCode(HASH_CODE_SEED, roleName, members);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::%s%s]",
        getClass().getName(),
        hashCode(),
        roleName,
        members);
  }
}
