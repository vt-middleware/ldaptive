/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.sasl.DefaultSaslClientRequest;
import org.ldaptive.sasl.SaslClientRequest;

/**
 * Interface for connection implementations.
 *
 * @author  Middleware Services
 */
public interface Connection extends AutoCloseable
{


  /**
   * Executes an abandon operation. Clients should execute abandons using {@link OperationHandle#abandon()}.
   *
   * @param  request  abandon request
   */
  void operation(AbandonRequest request);


  /**
   * Creates a handle for an add operation.
   *
   * @param  request  add request
   *
   * @return  operation handle
   */
  OperationHandle<AddRequest, AddResponse> operation(AddRequest request);


  /**
   * Creates a handle for a bind operation. Since clients must not send requests while a bind is in progress, some
   * methods may not be supported on the the operation handle.
   *
   * @param  request  bind request
   *
   * @return  operation handle
   */
  OperationHandle<BindRequest, BindResponse> operation(BindRequest request);


  /**
   * Creates a handle for a compare operation.
   *
   * @param  request  compare request
   *
   * @return  compare operation handle
   */
  CompareOperationHandle operation(CompareRequest request);


  /**
   * Creates a handle for an delete operation.
   *
   * @param  request  delete request
   *
   * @return  operation handle
   */
  OperationHandle<DeleteRequest, DeleteResponse> operation(DeleteRequest request);


  /**
   * Creates a handle for an extended operation.
   *
   * @param  request  extended request
   *
   * @return  extended operation handle
   */
  ExtendedOperationHandle operation(ExtendedRequest request);


  /**
   * Creates a handle for a modify operation.
   *
   * @param  request  modify request
   *
   * @return  operation handle
   */
  OperationHandle<ModifyRequest, ModifyResponse> operation(ModifyRequest request);


  /**
   * Creates a handle for a modify dn operation.
   *
   * @param  request  modify dn request
   *
   * @return  operation handle
   */
  OperationHandle<ModifyDnRequest, ModifyDnResponse> operation(ModifyDnRequest request);


  /**
   * Creates a handle for a search operation.
   *
   * @param  request  search request
   *
   * @return  search operation handle
   */
  SearchOperationHandle operation(SearchRequest request);


  /**
   * Returns the result of a SASL request that requires use of a generic SASL client.
   *
   * @param  request  SASL client request
   *
   * @return  operation result
   *
   * @throws  LdapException  if the operation fails or another bind is in progress
   */
  BindResponse operation(SaslClientRequest request) throws LdapException;


  /**
   * Returns the result of a SASL request that requires use of the default SASL client. This includes CRAM-MD5,
   * DIGEST-MD5, and GSS-API.
   *
   * @param  request  default SASL client request
   *
   * @return  operation result
   *
   * @throws  LdapException  if the operation fails or another bind is in progress
   */
  BindResponse operation(DefaultSaslClientRequest request) throws LdapException;


  /**
   * Returns whether this connection is open.
   *
   * @return  whether this connection is open
   */
  boolean isOpen();


  /**
   * Opens the connection.
   *
   * @throws  LdapException  if an error occurs opening the connection
   */
  void open() throws LdapException;


  @Override
  default void close()
  {
    close((RequestControl[]) null);
  }


  /**
   * Closes the connection.
   *
   * @param  controls  to send when closing the connection
   */
  void close(RequestControl... controls);
}
