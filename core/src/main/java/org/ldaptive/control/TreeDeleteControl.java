/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;

/**
 * Request control for TreeDelete. See draft-armijo-ldap-treedelete.
 *
 * @author  Middleware Services
 */
public class TreeDeleteControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.805";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 7027;


  /** Default constructor. */
  public TreeDeleteControl()
  {
    super(OID);
  }


  /**
   * Creates a new tree delete control.
   *
   * @param  critical  whether this control is critical
   */
  public TreeDeleteControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof TreeDeleteControl && super.equals(o);
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
