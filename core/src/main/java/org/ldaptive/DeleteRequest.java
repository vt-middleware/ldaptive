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
package org.ldaptive;

import java.util.Arrays;

/**
 * Contains the data required to perform an ldap delete operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DeleteRequest extends AbstractRequest
{

  /** DN to delete. */
  private String deleteDn;


  /** Default constructor. */
  public DeleteRequest() {}


  /**
   * Creates a new delete request.
   *
   * @param  dn  to delete
   */
  public DeleteRequest(final String dn)
  {
    setDn(dn);
  }


  /**
   * Returns the DN to delete.
   *
   * @return  DN
   */
  public String getDn()
  {
    return deleteDn;
  }


  /**
   * Sets the DN to delete.
   *
   * @param  dn  to delete
   */
  public void setDn(final String dn)
  {
    deleteDn = dn;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::deleteDn=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        deleteDn,
        Arrays.toString(getControls()));
  }
}
