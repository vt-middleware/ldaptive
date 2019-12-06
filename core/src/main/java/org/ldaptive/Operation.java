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
   * Sends an asynchronous request and does not wait for a response.
   *
   * @param  request  operation request
   *
   * @return  operation result
   *
   * @throws  LdapException  if the operation fails
   */
  OperationHandle<Q, S> send(Q request) throws LdapException;


  /**
   * Sends an asynchronous request and waits for the response.
   *
   * @param  request  operation request
   *
   * @return  operation result
   *
   * @throws  LdapException  if the operation fails
   */
  S execute(Q request) throws LdapException;
}
