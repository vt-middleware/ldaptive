/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.ldaptive.LdapUtils;

/**
 * Provides a custom implementation for grouping principals.
 *
 * @author  Middleware Services
 */
public class LdapGroup implements Principal, Serializable
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 431;

  /** serial version uid. */
  private static final long serialVersionUID = 7947097145323474960L;

  /** LDAP group name. */
  private final String groupName;

  /** Principal members. */
  private final Set<Principal> members = new HashSet<>();


  /**
   * Creates a new ldap group with the supplied name.
   *
   * @param  name  of the group
   */
  public LdapGroup(final String name)
  {
    groupName = name;
  }


  @Override
  public String getName()
  {
    return groupName;
  }


  /**
   * Adds a member to this group.
   *
   * @param  user  to add
   */
  public void addMember(final Principal user)
  {
    members.add(user);
  }


  /**
   * Removes a member from this group.
   *
   * @param  user  to remove
   */
  public void removeMember(final Principal user)
  {
    members.remove(user);
  }


  public boolean isMember(final Principal member)
  {
    for (Principal p : members) {
      if (p.getName() != null && p.getName().equals(member.getName())) {
        return true;
      }
    }
    return false;
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


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapGroup) {
      final LdapGroup v = (LdapGroup) o;
      return LdapUtils.areEqual(groupName, v.groupName) &&
             LdapUtils.areEqual(members, v.members);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, groupName, members);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("groupName=").append(groupName).append(", ")
      .append("members=").append(members).append("]").toString();
  }
}
