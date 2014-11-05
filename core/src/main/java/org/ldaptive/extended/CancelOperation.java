/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Executes an ldap cancel operation. See RFC 3909.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class CancelOperation extends AbstractOperation<CancelRequest, Void>
{


  /**
   * Creates a new cancel operation.
   *
   * @param  conn  connection
   */
  public CancelOperation(final Connection conn)
  {
    super(conn);
  }


  /** {@inheritDoc} */
  @Override
  protected Response<Void> invoke(final CancelRequest request)
    throws LdapException
  {
    @SuppressWarnings("unchecked") final Response<Void> response =
      (Response<Void>)
        getConnection().getProviderConnection().extendedOperation(request);
    return response;
  }
}
