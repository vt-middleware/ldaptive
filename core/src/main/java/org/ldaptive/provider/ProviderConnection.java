/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.AddRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.CompareRequest;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Response;
import org.ldaptive.SearchRequest;
import org.ldaptive.control.RequestControl;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.UnsolicitedNotificationListener;

/**
 * Interface for a provider specific implementation of ldap operations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ProviderConnection
{


  /**
   * Bind to the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the bind operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<Void> bind(BindRequest request)
    throws LdapException;


  /**
   * Add an entry to an ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the add operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<Void> add(AddRequest request)
    throws LdapException;


  /**
   * Compare an entry in the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the compare operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<Boolean> compare(CompareRequest request)
    throws LdapException;


  /**
   * Delete an entry in the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the delete operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<Void> delete(DeleteRequest request)
    throws LdapException;


  /**
   * Modify an entry in the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the modify operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<Void> modify(ModifyRequest request)
    throws LdapException;


  /**
   * Modify the DN of an entry in the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the modify dn operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<Void> modifyDn(ModifyDnRequest request)
    throws LdapException;


  /**
   * Search the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  search iterator
   *
   * @throws  LdapException  if an error occurs
   */
  SearchIterator search(SearchRequest request)
    throws LdapException;


  /**
   * Search the ldap asynchronously.
   *
   * @param  request  containing the data necessary to perform the operation
   * @param  listener  to be notified as results arrive
   *
   * @throws  LdapException  if an error occurs
   */
  void searchAsync(SearchRequest request, SearchListener listener)
    throws LdapException;


  /**
   * Abandon an operation.
   *
   * @param  messageId  of the operation to abandon
   * @param  controls  request controls
   *
   * @throws  LdapException  if an error occurs
   */
  void abandon(int messageId, RequestControl[] controls)
    throws LdapException;


  /**
   * Perform an extended operation in the ldap.
   *
   * @param  request  containing the data necessary to perform the operation
   *
   * @return  response associated with the extended operation
   *
   * @throws  LdapException  if an error occurs
   */
  Response<?> extendedOperation(ExtendedRequest request)
    throws LdapException;


  /**
   * Adds a listener to receive unsolicited notifications.
   *
   * @param  listener  to receive unsolicited notifications
   */
  void addUnsolicitedNotificationListener(
    UnsolicitedNotificationListener listener);


  /**
   * Removes a listener from receiving unsolicited notifications.
   *
   * @param  listener  that was registered to receive unsolicited notifications
   */
  void removeUnsolicitedNotificationListener(
    UnsolicitedNotificationListener listener);


  /**
   * Tear down this connection to an LDAP.
   *
   * @param  controls  request controls
   *
   * @throws  LdapException  if an LDAP error occurs
   */
  void close(RequestControl[] controls)
    throws LdapException;
}
