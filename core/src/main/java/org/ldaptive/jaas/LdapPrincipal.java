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
public class LdapPrincipal
  implements Principal, Serializable, Comparable<Principal>
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 419;

  /** serial version uid. */
  private static final long serialVersionUID = 762147223399104252L;

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


  /** {@inheritDoc} */
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
    return LdapUtils.computeHashCode(HASH_CODE_SEED, ldapName);
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
        ldapName,
        ldapEntry != null ? ldapEntry : "");
  }


  /** {@inheritDoc} */
  @Override
  public int compareTo(final Principal p)
  {
    return ldapName.compareTo(p.getName());
  }
}
