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
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.control.AbstractControl;
import org.ldaptive.control.RequestControl;

/**
 * Request control for active directory servers to avoid error response with
 * range retrieval. See http://msdn.microsoft.com/en-us/library/cc223345.aspx
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class RangeRetrievalNoerrControl extends AbstractControl
  implements RequestControl
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
