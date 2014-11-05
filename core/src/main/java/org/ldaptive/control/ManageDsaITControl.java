/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;

/**
 * Request control for ManageDsaIT. See RFC 3296.
 *
 * @author  Middleware Services
 */
public class ManageDsaITControl extends AbstractControl
  implements RequestControl
{

  /** OID of this control. */
  public static final String OID = "2.16.840.1.113730.3.4.2";

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 701;


  /** Default constructor. */
  public ManageDsaITControl()
  {
    super(OID);
  }


  /**
   * Creates a new ManageDsaIT control.
   *
   * @param  critical  whether this control is critical
   */
  public ManageDsaITControl(final boolean critical)
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
