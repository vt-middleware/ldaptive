/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import org.ldaptive.control.RequestControl;
import org.ldaptive.protocol.AbandonRequest;
import org.ldaptive.protocol.AddRequest;
import org.ldaptive.protocol.BindRequest;
import org.ldaptive.protocol.CompareRequest;
import org.ldaptive.protocol.DeleteRequest;
import org.ldaptive.protocol.ExtendedRequest;
import org.ldaptive.protocol.ModifyDnRequest;
import org.ldaptive.protocol.ModifyRequest;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.SearchRequest;
import org.ldaptive.protocol.UnbindRequest;

/**
 * Base class for connection implementations.
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
abstract class Connection implements AutoCloseable
// CheckStyle:AbstractClassName ON
{


  /**
   * Executes an abandon operation. Clients should execute abandons using {@link OperationHandle#abandon()}.
   *
   * @param  request  abandon request
   */
  public abstract void operation(AbandonRequest request);


  /**
   * Creates a handle for an add operation.
   *
   * @param  request  add request
   *
   * @return  operation handle
   */
  public abstract OperationHandle operation(AddRequest request);


  /**
   * Creates a handle for a compare operation.
   *
   * @param  request  compare request
   *
   * @return  compare operation handle
   */
  public abstract CompareOperationHandle operation(CompareRequest request);


  /**
   * Creates a handle for an delete operation.
   *
   * @param  request  delete request
   *
   * @return  operation handle
   */
  public abstract OperationHandle operation(DeleteRequest request);


  /**
   * Creates a handle for an extended operation.
   *
   * @param  request  extended request
   *
   * @return  extended operation handle
   */
  public abstract ExtendedOperationHandle operation(ExtendedRequest request);


  /**
   * Creates a handle for a modify operation.
   *
   * @param  request  modify request
   *
   * @return  operation handle
   */
  public abstract OperationHandle operation(ModifyRequest request);


  /**
   * Creates a handle for a modify dn operation.
   *
   * @param  request  modify dn request
   *
   * @return  operation handle
   */
  public abstract OperationHandle operation(ModifyDnRequest request);


  /**
   * Creates a handle for a search operation.
   *
   * @param  request  search request
   *
   * @return  search operation handle
   */
  public abstract SearchOperationHandle operation(SearchRequest request);


  /**
   * Performs a bind operation. Bind operations are synchronous since they affect the security context of the
   * connection. Other operations must block until the bind response is received.
   *
   * @param  request  bind request
   *
   * @return  bind operation result
   *
   * @throws  LdapException  if the bind operation fails
   */
  public abstract Result operation(BindRequest request)
    throws LdapException;


  /**
   * Opens the connection.
   *
   * @throws  LdapException  if an error occurs opening the connection
   */
  public abstract void open()
    throws LdapException;


  @Override
  public void close()
  {
    close(null);
  }


  /**
   * Closes the connection.
   *
   * @param  controls  to send when closing the connection
   */
  public abstract void close(RequestControl... controls);


  /**
   * Executes an unbind operation. Clients should close connections using {@link #close()}.
   *
   * @param  request  unbind request
   */
  abstract void operation(UnbindRequest request);


  /**
   * Write the request in the supplied handle to the LDAP server. This method does not throw, it should report
   * exceptions to the handle.
   *
   * @param  handle  for the operation write
   */
  abstract void write(OperationHandle handle);
}
