/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.persistence;

import org.ldaptive.AddResponse;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapException;
import org.ldaptive.Result;

/**
 * Interface to manage objects that have been annotated to contain LDAP data.
 *
 * @param  <T>  type of object to manage
 *
 * @author  Middleware Services
 */
public interface LdapEntryManager<T>
{


  /**
   * Searches for the supplied annotated object in an LDAP and returns the object mapped with its ldap attribute
   * properties set.
   *
   * @param  object  to find
   *
   * @return  mapped object
   *
   * @throws  LdapException  if the object cannot be found
   */
  T find(T object)
    throws LdapException;


  /**
   * Adds the supplied annotated object to an LDAP.
   *
   * @param  object  to add
   *
   * @return  LDAP response from the add operation
   *
   * @throws  LdapException  if the add fails
   */
  AddResponse add(T object)
    throws LdapException;


  /**
   * Merges the supplied annotated object in an LDAP. See {@link org.ldaptive.ext.MergeOperation}.
   *
   * @param  object  to merge
   *
   * @return  LDAP response from the merge operation
   *
   * @throws  LdapException  if the merge fails
   */
  Result merge(T object)
    throws LdapException;


  /**
   * Deletes the supplied annotated object from an LDAP.
   *
   * @param  object  to delete
   *
   * @return  LDAP response from the delete operation
   *
   * @throws  LdapException  if the delete fails
   */
  DeleteResponse delete(T object)
    throws LdapException;
}
