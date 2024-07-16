/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

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
public class LdapGroup implements Principal
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 431;

  /** LDAP group name. */
  private final String groupName;

  /** Principal members. */
  private final Set<Principal> members = new HashSet<>();


  /**
   * Creates a new ldap group with the supplied name.
   *
   * @param  name  of the group
   * @param  principals  that are members of the group
   */
  public LdapGroup(final String name, final Set<Principal> principals)
  {
    groupName = name;
    members.addAll(principals);
  }


  @Override
  public String getName()
  {
    return groupName;
  }


  /**
   * Returns whether the supplied member is in this group.
   *
   * @param  member  to check
   * @return  whether the supplied member is in this group
   */
  public boolean isMember(final Principal member)
  {
    return members.stream().anyMatch(member::equals);
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
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "groupName=" + groupName + ", " +
      "members=" + members + "]";
  }
}
