/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.io.Serializable;
import java.security.Principal;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Provides a custom implementation for adding LDAP principals to a subject.
 *
 * @author  Middleware Services
 */
public class LdapDnPrincipal
  implements Principal, Serializable, Comparable<Principal>
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 409;

  /** serial version uid. */
  private static final long serialVersionUID = 8345846704852267195L;

  /** LDAP user name. */
  private final String ldapDn;

  /** User ldap entry. */
  private final LdapEntry ldapEntry;


  /**
   * Creates a new ldap principal with the supplied name.
   *
   * @param  name  of an ldap DN
   * @param  entry  ldap entry associated with this principal
   */
  public LdapDnPrincipal(final String name, final LdapEntry entry)
  {
    ldapDn = name;
    ldapEntry = entry;
  }


  @Override
  public String getName()
  {
    return ldapDn;
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
    return LdapUtils.areEqual(this, o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, ldapDn);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::%s%s]",
        getClass().getName(),
        hashCode(),
        ldapDn,
        ldapEntry != null ? ldapEntry : "");
  }


  @Override
  public int compareTo(final Principal p)
  {
    return ldapDn.compareTo(p.getName());
  }
}
