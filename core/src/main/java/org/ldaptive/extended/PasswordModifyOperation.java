/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.Credential;
import org.ldaptive.LdapException;
import org.ldaptive.Response;

/**
 * Executes an ldap password modify operation. See RFC 3062.
 *
 * @author  Middleware Services
 */
public class PasswordModifyOperation
  extends AbstractOperation<PasswordModifyRequest, Credential>
{


  /**
   * Creates a new password modify operation.
   *
   * @param  conn  connection
   */
  public PasswordModifyOperation(final Connection conn)
  {
    super(conn);
  }


  @Override
  protected Response<Credential> invoke(final PasswordModifyRequest request)
    throws LdapException
  {
    @SuppressWarnings("unchecked") final Response<Credential> response =
      (Response<Credential>)
        getConnection().getProviderConnection().extendedOperation(request);
    return response;
  }
}
