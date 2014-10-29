/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.jaas;

import java.io.Serializable;
import org.ldaptive.LdapUtils;

/**
 * Provides a custom implementation for adding LDAP credentials to a subject.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class LdapCredential implements Serializable
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 401;

  /** serial version uid. */
  private static final long serialVersionUID = 1965949592374728021L;

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
    return LdapUtils.computeHashCode(HASH_CODE_SEED, credential);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::%s]",
        getClass().getName(),
        hashCode(),
        credential);
  }
}
