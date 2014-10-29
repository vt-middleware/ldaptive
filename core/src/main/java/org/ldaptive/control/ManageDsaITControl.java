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
package org.ldaptive.control;

import org.ldaptive.LdapUtils;

/**
 * Request control for ManageDsaIT. See RFC 3296.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
