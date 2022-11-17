/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes callable tasks asynchronously.
 *
 * @param  <T>  type of result from the callable
 *
 * @author  Middleware Services
 */
public class CallableWorker<T>
{

  /** Default size of the thread pool. */
  private static final int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors() * 2;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Executor service. */
  private final ExecutorService executorService;


  /**
   * Creates a new callable worker with a fixed sized thread pool. The size of the thread pool is set to twice the
   * number of available processors. See {@link Runtime#availableProcessors()}.
   *
   * @param  poolName  name to designate on the thread pool
   */
  public CallableWorker(final String poolName)
  {
    this(poolName, DEFAULT_NUM_THREADS);
  }


  /**
   * Creates a new callable worker with a fixed sized thread pool.
   *
   * @param  poolName  name to designate on the thread pool
   * @param  numThreads  size of the thread pool
   */
  public CallableWorker(final String poolName, final int numThreads)
  {
    executorService = Executors.newFixedThreadPool(
      numThreads,
      r -> {
        final Thread t = new Thread(r, "ldaptive-" + poolName + "@" + hashCode());
        t.setDaemon(true);
        return t;
      });
  }


  /**
   * Creates a new callable worker.
   *
   * @param  es  executor service to run callables
   */
  public CallableWorker(final ExecutorService es)
  {
    executorService = es;
  }


  /**
   * Executes all callables and provides each result to the supplied consumer.
   *
   * @param  callable  callable to execute
   * @param  count  number of times to execute the supplied callable
   * @param  consumer  to process callable results
   *
   * @return  list of exceptions thrown during the execution
   */
  public List<ExecutionException> execute(final Callable<T> callable, final int count, final Consumer<T> consumer)
  {
    return execute(IntStream.range(0, count).mapToObj(i -> callable).collect(Collectors.toList()), consumer);
  }


  /**
   * Executes all callables and provides each result to the supplied consumer. Note that the consumer is invoked in a
   * synchronous fashion, waiting for each result from the callables.
   *
   * @param  callables  callables to execute
   * @param  consumer  to process callable results
   *
   * @return  list of exceptions thrown during the execution
   */
  public List<ExecutionException> execute(final List<Callable<T>> callables, final Consumer<T> consumer)
  {
    final CompletionService<T> cs = new ExecutorCompletionService<>(executorService);
    callables.stream().forEach(cs::submit);
    final List<ExecutionException> exceptions = new ArrayList<>(callables.size());
    for (int i = 0; i < callables.size(); i++) {
      try {
        // blocks until a result is received
        final T result = cs.take().get();
        consumer.accept(result);
      } catch (ExecutionException e) {
        exceptions.add(e);
      } catch (InterruptedException e) {
        logger.warn("Concurrent execution interrupted", e);
        exceptions.add(new ExecutionException(e));
      }
    }
    return exceptions;
  }


  /**
   * Shutdown the underlying executor service.
   */
  public void shutdown()
  {
    executorService.shutdown();
  }
}
