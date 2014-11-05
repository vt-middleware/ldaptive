/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Executes an ldap who am i operation. See RFC 4532.
 *
 * @author  Middleware Services
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
