/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers to avoid error response with range retrieval. See
 * http://msdn.microsoft.com/en-us/library/cc223345.aspx
 *
 * @author  Middleware Services
 */
public class RangeRetrievalNoerrControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.1948";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 983;


  /** Default constructor. */
  public RangeRetrievalNoerrControl()
  {
    super(OID);
  }


  /**
   * Creates a new notification control.
   *
   * @param  critical  whether this control is critical
   */
  public RangeRetrievalNoerrControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof RangeRetrievalNoerrControl && super.equals(o);
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
