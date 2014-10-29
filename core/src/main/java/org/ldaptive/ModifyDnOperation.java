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

/**
 * Executes an ldap modify dn operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ModifyDnOperation extends AbstractOperation<ModifyDnRequest, Void>
{


  /**
   * Creates a new modify dn operation.
   *
   * @param  conn  connection
   */
  public ModifyDnOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<Void> invoke(final ModifyDnRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().modifyDn(request);
  }
}
