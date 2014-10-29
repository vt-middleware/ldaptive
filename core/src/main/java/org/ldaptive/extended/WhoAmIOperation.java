/*
  $Id: WhoAmIOperation.java 2885 2014-02-05 21:28:49Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2885 $
  Updated: $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
*/
package org.ldaptive.extended;

import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Executes an ldap who am i operation. See RFC 4532.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class WhoAmIOperation extends AbstractOperation<WhoAmIRequest, String>
{


  /**
   * Creates a new who am i operation.
   *
   * @param  conn  connection
   */
  public WhoAmIOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<String> invoke(final WhoAmIRequest request)
    throws LdapException
  {
    @SuppressWarnings("unchecked") final Response<String> response =
      (Response<String>)
        getConnection().getProviderConnection().extendedOperation(request);
    return response;
  }
}
