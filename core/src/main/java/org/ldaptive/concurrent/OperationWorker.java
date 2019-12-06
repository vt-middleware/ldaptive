/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.Collection;
import org.ldaptive.OperationHandle;
import org.ldaptive.Request;
import org.ldaptive.Result;

/**
 * Interface for ldap operation workers. These interface is meant to facilitate executing multiple requests and
 * processing multiple responses.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public interface OperationWorker<Q extends Request, S extends Result>
{


  /**
   * Execute an ldap operation for each request.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  handle responses for this operation
   */
  Collection<OperationHandle<Q, S>> send(Q[] requests);


  /**
   * Execute an ldap operation for each request and waits for each operation to complete.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  responses for this operation
   */
  Collection<S> execute(Q[] requests);
}
