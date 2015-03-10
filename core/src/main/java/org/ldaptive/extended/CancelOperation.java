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


  @Override
  protected Response<Void> invoke(final CancelRequest request)
    throws LdapException
  {
    @SuppressWarnings("unchecked")
    final Response<Void> response = (Response<Void>) getConnection().getProviderConnection().extendedOperation(request);
    return response;
  }
}
