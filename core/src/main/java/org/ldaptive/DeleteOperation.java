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
 * Executes an ldap delete operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DeleteOperation extends AbstractOperation<DeleteRequest, Void>
{


  /**
   * Creates a new delete operation.
   *
   * @param  conn  connection
   */
  public DeleteOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<Void> invoke(final DeleteRequest request)
    throws LdapException
  {
    return getConnection().getProviderConnection().delete(request);
  }
}
