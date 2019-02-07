/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.ldaptive.Operation;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for worker operations. If no {@link ExecutorService} is provided a cached thread pool is used by default.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public abstract class AbstractOperationWorker<Q extends Request, S extends Result> implements OperationWorker<Q, S>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** operation to execute. */
  private final Operation<Q, S> operation;

  /** to submit operations to. */
  private final ExecutorService service;


  /**
   * Creates a new abstract operation worker.
   *
   * @param  op  operation
   */
  public AbstractOperationWorker(final Operation<Q, S> op)
  {
    this(op, Executors.newCachedThreadPool());
  }


  /**
   * Creates a new abstract operation worker.
   *
   * @param  op  operation
   * @param  es  executor service
   */
  public AbstractOperationWorker(final Operation<Q, S> op, final ExecutorService es)
  {
    operation = op;
    service = es;
  }


  /**
   * Execute an ldap operation on a separate thread.
   *
   * @param  request  containing the data required by this operation
   *
   * @return  future response for this operation
   */
  @Override
  public Future<S> execute(final Q request)
  {
    return service.submit(() -> operation.execute(request));
  }


  /**
   * Execute an ldap operation for each request on a separate thread.
   *
   * @param  requests  containing the data required by this operation
   *
   * @return  future responses for this operation
   */
  @Override
  @SuppressWarnings("unchecked")
  public Collection<Future<S>> execute(final Q... requests)
  {
    final List<Future<S>> results = new ArrayList<>(requests.length);
    for (Q request : requests) {
      results.add(service.submit(() -> operation.execute(request)));
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
  @SuppressWarnings("unchecked")
  public Collection<S> executeToCompletion(final Q... requests)
  {
    final CompletionService<S> cs = new ExecutorCompletionService<>(service);
    final List<Future<S>> futures = new ArrayList<>(requests.length);
    for (Q request : requests) {
      futures.add(cs.submit(() -> operation.execute(request)));
    }

    final List<S> responses = new ArrayList<>(requests.length);
    for (Future<S> future : futures) {
      try {
        responses.add(future.get());
      } catch (ExecutionException e) {
        logger.debug("ExecutionException thrown, ignoring", e);
      } catch (InterruptedException e) {
        logger.warn("InterruptedException thrown, ignoring", e);
      }
    }
    return responses;
  }


  /** Invokes {@link ExecutorService#shutdown()} on the underlying executor service. */
  public void shutdown()
  {
    service.shutdown();
  }
}
