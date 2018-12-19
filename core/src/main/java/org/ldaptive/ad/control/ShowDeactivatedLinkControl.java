/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers in include link attributes that refer to deleted-objects in a search
 * operation. See http://msdn.microsoft.com/en-us/library/dd302781.aspx
 *
 * @author  Middleware Services
 */
public class ShowDeactivatedLinkControl extends AbstractControl implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.2065";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 967;


  /** Default constructor. */
  public ShowDeactivatedLinkControl()
  {
    super(OID);
  }


  /**
   * Creates a new show deactivated link control.
   *
   * @param  critical  whether this control is critical
   */
  public ShowDeactivatedLinkControl(final boolean critical)
  {
    super(OID, critical);
  }


  @Override
  public boolean hasValue()
  {
    return false;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    return o instanceof ShowDeactivatedLinkControl && super.equals(o);
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
