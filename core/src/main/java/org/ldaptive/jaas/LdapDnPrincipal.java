/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.security.Principal;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.auth.AccountState;

/**
 * Provides a custom implementation for adding LDAP principals to a subject.
 *
 * @author  Middleware Services
 */
public class LdapDnPrincipal implements Principal, Comparable<Principal>
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 409;

  /** LDAP user name. */
  private final String ldapDn;

  /** User ldap entry. */
  private final LdapEntry ldapEntry;

  /** User account state. */
  private final AccountState accountState;


  /**
   * Creates a new ldap principal with the supplied name.
   *
   * @param  name  of an ldap DN
   * @param  entry  ldap entry associated with this principal
   */
  public LdapDnPrincipal(final String name, final LdapEntry entry)
  {
    this(name, entry, null);
  }


  /**
   * Creates a new ldap principal with the supplied name.
   *
   * @param  name  of an ldap DN
   * @param  entry  ldap entry associated with this principal
   * @param  state  account state associated with this principal
   */
  public LdapDnPrincipal(final String name, final LdapEntry entry, final AccountState state)
  {
    ldapDn = LdapUtils.assertNotNullArgOr(name, String::isEmpty, "Name cannot be null or empty");
    ldapEntry = entry;
    accountState = state;
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


  /**
   * Returns the account state for this ldap principal.
   *
   * @return  account state
   */
  public AccountState getAccountState()
  {
    return accountState;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapDnPrincipal) {
      final LdapDnPrincipal v = (LdapDnPrincipal) o;
      return LdapUtils.areEqual(ldapDn, v.ldapDn);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, ldapDn);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "ldapDn=" + ldapDn + ", " +
      "ldapEntry=" + (ldapEntry != null ? ldapEntry : "") + ", " +
      "accountState=" + (accountState != null ? accountState : "") + "]";
  }


  @Override
  public int compareTo(final Principal p)
  {
    return ldapDn.compareTo(p.getName());
  }
}
