/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface for ldap operations.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public interface Operation<Q extends Request, S>
{


  /**
   * Execute this ldap operation.
   *
   * @param  request  containing the data required by this operation
   *
   * @return  response for this operation
   *
   * @throws  LdapException  if the operation fails
   */
  Response<S> execute(Q request)
    throws LdapException;
}
