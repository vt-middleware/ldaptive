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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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


  /** {@inheritDoc} */
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
