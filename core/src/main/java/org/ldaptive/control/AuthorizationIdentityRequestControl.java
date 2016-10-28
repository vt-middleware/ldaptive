/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;

/**
 * Request control for authorization identify. See RFC 3829.
 *
 * @author  Middleware Services
 */
public class AuthorizationIdentityRequestControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.16";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7013;


  /** Default constructor. */
  public AuthorizationIdentityRequestControl()
  {
    super(OID);
  }


  /**
   * Creates a new ManageDsaIT control.
   *
   * @param  critical  whether this control is critical
   */
  public AuthorizationIdentityRequestControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof AuthorizationIdentityRequestControl && super.equals(o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality());
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d::criticality=%s]", getClass().getName(), hashCode(), getCriticality());
  }


  @Override
  public byte[] encode()
  {
    return null;
  }
}
