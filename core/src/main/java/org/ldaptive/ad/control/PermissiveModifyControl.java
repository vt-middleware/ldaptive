/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers to return success on
 * add/modify/delete operations that would normally return an error. See
 * http://msdn.microsoft.com/en-us/library/cc223352.aspx
 *
 * @author  Middleware Services
 */
public class PermissiveModifyControl extends AbstractControl
  implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "1.2.840.113556.1.4.1413";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 941;


  /** Default constructor. */
  public PermissiveModifyControl()
  {
    super(OID);
  }


  /**
   * Creates a new permissive modify control.
   *
   * @param  critical  whether this control is critical
   */
  public PermissiveModifyControl(final boolean critical)
  {
    super(OID, critical);
  }


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(HASH_CODE_SEED, getOID(), getCriticality());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::criticality=%s]",
        getClass().getName(),
        hashCode(),
        getCriticality());
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encode()
  {
    return null;
  }
}
