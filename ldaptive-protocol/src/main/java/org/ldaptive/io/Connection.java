/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import org.ldaptive.control.RequestControl;
import org.ldaptive.protocol.AbandonRequest;
import org.ldaptive.protocol.CompareRequest;
import org.ldaptive.protocol.ExtendedRequest;
import org.ldaptive.protocol.Request;
import org.ldaptive.protocol.SearchRequest;
import org.ldaptive.protocol.UnbindRequest;

/**
 * Base class for connection implementations.
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
abstract class Connection
// CheckStyle:AbstractClassName ON
{


  /**
   * Creates a handle for a search operation.
   *
   * @param  request  search request
   *
   * @return  search operation handle
   */
  public abstract SearchOperationHandle operation(SearchRequest request);


  /**
   * Creates a handle for a compare operation.
   *
   * @param  request  compare request
   *
   * @return  compare operation handle
   */
  public abstract CompareOperationHandle operation(CompareRequest request);


  /**
   * Creates a handle for an extended operation.
   *
   * @param  request  extended request
   *
   * @return  extended operation handle
   */
  public abstract ExtendedOperationHandle operation(ExtendedRequest request);


  /**
   * Creates a handle for an LDAP operation.
   *
   * @param  request  LDAP request
   *
   * @return  operation handle
   */
  public abstract OperationHandle operation(Request request);


  /**
   * Closes the connection.
   *
   * @param  controls  to send when closing the connection
   */
  public abstract void close(RequestControl... controls);


  /**
   * Executes an unbind operation.
   *
   * @param  request  unbind request
   */
  protected abstract void operation(UnbindRequest request);


  /**
   * Executes an abandon operation.
   *
   * @param  request  abandon request
   */
  protected abstract void operation(AbandonRequest request);


  /**
   * Write the request in the supplied handle to the LDAP server.
   *
   * @param  handle  for the operation write
   */
  protected abstract void write(OperationHandle handle);
}
