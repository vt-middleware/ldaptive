/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers to perform an update even if the data is already the same. See
 * http://msdn.microsoft.com/en-us/library/cc223344.aspx
 *
 * @author  Middleware Services
 */
public class ForceUpdateControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.1974";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 971;


  /** Default constructor. */
  public ForceUpdateControl()
  {
    super(OID);
  }


  /**
   * Creates a new force update control.
   *
   * @param  critical  whether this control is critical
   */
  public ForceUpdateControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof ForceUpdateControl && super.equals(0);
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
