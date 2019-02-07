/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Operation interface.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public interface Operation<Q extends Request, S extends Result>
{


  /**
   * Executes a request.
   *
   * @param  request  operation request
   *
   * @return  operation result
   *
   * @throws  LdapException  if the operation fails
   */
  S execute(Q request) throws LdapException;
}
