/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import org.ldaptive.LdapUtils;

/**
 * Provides a custom implementation for adding LDAP credentials to a subject.
 *
 * @author  Middleware Services
 */
public class LdapCredential
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 401;

  /** LDAP credential. */
  private final Object credential;


  /**
   * Creates a new ldap credential with the supplied credential.
   *
   * @param  o  credential to store
   */
  public LdapCredential(final Object o)
  {
    credential = o;
  }


  /**
   * Returns the credential for this ldap credential.
   *
   * @return  credential
   */
  public Object getCredential()
  {
    return credential;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof LdapCredential) {
      final LdapCredential v = (LdapCredential) o;
      return LdapUtils.areEqual(credential, v.credential);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, credential);
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "credential=" + credential + "]";
  }
}
