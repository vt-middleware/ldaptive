/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.security.Principal;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Provides a custom implementation for adding LDAP principals to a subject.
 *
 * @author  Middleware Services
 */
public class LdapPrincipal implements Principal, Comparable<Principal>
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 419;

  /** LDAP user name. */
  private final String ldapName;

  /** User ldap entry. */
  private final LdapEntry ldapEntry;


  /**
   * Creates a new ldap principal with the supplied name.
   *
   * @param  name  of this principal
   * @param  entry  ldap entry associated with this principal
   */
  public LdapPrincipal(final String name, final LdapEntry entry)
  {
    ldapName = name;
    ldapEntry = entry;
  }


  @Override
  public String getName()
  {
    return ldapName;
  }


  /**
   * Returns the ldap entry for this ldap principal.
   *
   * @return  ldap entry
   */
  public LdapEntry getLdapEntry()
  {
    return ldapEntry;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapPrincipal) {
      final LdapPrincipal v = (LdapPrincipal) o;
      return LdapUtils.areEqual(ldapName, v.ldapName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, ldapName);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "ldapName=" + ldapName + ", " +
      "ldapEntry=" + (ldapEntry != null ? ldapEntry : "") + "]";
  }


  @Override
  public int compareTo(final Principal p)
  {
    return ldapName.compareTo(p.getName());
  }
}
