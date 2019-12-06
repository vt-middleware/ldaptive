/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ldaptive.LdapException;
import org.ldaptive.Operation;
import org.ldaptive.OperationHandle;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for worker operations.
 *
 * @param  <T>  type of operation
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public abstract class AbstractOperationWorker<T extends Operation<Q , S>, Q extends Request, S extends Result>
  implements OperationWorker<Q, S>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** operation to execute. */
  private T operation;


  /**
   * Creates a new abstract operation worker.
   *
   * @param  op  operation
   */
  public AbstractOperationWorker(final T op)
  {
    setOperation(op);
  }


  /**
   * Returns the underlying operation.
   *
   * @return  operation
   */
  public T getOperation()
  {
    return operation;
  }


  /**
   * Sets the underlying operation.
   *
   * @param  op  to set
   */
  public void setOperation(final T op)
  {
    operation = op;
  }


  /**
   * Execute an ldap operation for each request on a separate thread.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  future responses for this operation
   */
  @Override
  public Collection<OperationHandle<Q, S>> send(final Q[] requests)
  {
    final List<OperationHandle<Q, S>> results = new ArrayList<>(requests.length);
    for (Q request : requests) {
      try {
        results.add(operation.send(request));
      } catch (LdapException e) {
        logger.warn("Error occurred attempting to execute request {}", request, e);
      }
    }
    return results;
  }


  /**
   * Execute an ldap operation for each request on a separate thread and waits for all operations to complete.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  responses for this operation
   */
  @Override
  public Collection<S> execute(final Q[] requests)
  {
    final List<S> responses = new ArrayList<>(requests.length);
    final Collection<OperationHandle<Q, S>> handles = send(requests);
    for (OperationHandle<Q, S> handle : handles) {
      try {
        responses.add(handle.await());
      } catch (LdapException e) {
        logger.warn("Error occurred waiting on handle {}", handle, e);
      }
    }
    return responses;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("operation=").append(operation).toString();
  }
}
