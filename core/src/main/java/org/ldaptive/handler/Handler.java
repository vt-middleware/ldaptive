/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;

/**
 * Interface for ldap handlers.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public interface Handler<Q extends Request, S>
{


  /**
   * Handle the supplied result.
   *
   * @param  conn  connection the operation was executed on
   * @param  request  executed by the operation
   * @param  result  produced from the operation
   *
   * @return  handler result
   *
   * @throws  LdapException  if handling fails
   */
  HandlerResult<S> handle(Connection conn, Q request, S result)
    throws LdapException;
}
