/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Interface for ldap operations.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
