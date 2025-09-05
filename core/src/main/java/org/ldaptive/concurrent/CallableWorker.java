/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ldaptive.LdapUtils;
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

  /** Maximum task queue size, value is {@value}. */
  private static final int MAX_QUEUE_SIZE = 65535;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Executor service. */
  private final ExecutorService executorService;

  /** Time to wait for results. */
  private final Duration timeout;


  /**
   * Creates a new callable worker with a fixed sized thread pool. The size of the thread pool is set to twice the
   * number of available processors. See {@link Runtime#availableProcessors()}.
   *
   * @param  poolName  name to designate on the thread pool
   */
  public CallableWorker(final String poolName)
  {
    this(poolName, DEFAULT_NUM_THREADS, Duration.ZERO);
  }


  /**
   * Creates a new callable worker with a fixed sized thread pool. The size of the thread pool is set to twice the
   * number of available processors. See {@link Runtime#availableProcessors()}.
   *
   * @param  poolName  name to designate on the thread pool
   * @param  time  to wait for a result before interrupting execution
   */
  public CallableWorker(final String poolName, final Duration time)
  {
    this(poolName, DEFAULT_NUM_THREADS, time);
  }


  /**
   * Creates a new callable worker with a fixed sized thread pool.
   *
   * @param  poolName  name to designate on the thread pool
   * @param  numThreads  size of the thread pool
   */
  public CallableWorker(final String poolName, final int numThreads)
  {
    this(poolName, numThreads, Duration.ZERO);
  }


  /**
   * Creates a new callable worker with a fixed sized thread pool.
   *
   * @param  poolName  name to designate on the thread pool
   * @param  numThreads  size of the thread pool
   * @param  time  to wait for a result before interrupting execution
   */
  public CallableWorker(final String poolName, final int numThreads, final Duration time)
  {
    timeout = LdapUtils.assertNotNullArgOr(time, Duration::isNegative, "Timeout cannot be null or negative");
    executorService = new ThreadPoolExecutor(
      numThreads,
      numThreads,
      0L,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(MAX_QUEUE_SIZE),
      r -> {
        final Thread t = new Thread(r, "ldaptive-" + poolName + "@" + hashCode());
        t.setDaemon(true);
        return t;
      },
      new ThreadPoolExecutor.AbortPolicy());
  }


  /**
   * Creates a new callable worker.
   *
   * @param  es  executor service to run callables
   */
  public CallableWorker(final ExecutorService es)
  {
    this(es, Duration.ZERO);
  }


  /**
   * Creates a new callable worker.
   *
   * @param  es  executor service to run callables
   * @param  time  to wait for a result before interrupting execution
   */
  public CallableWorker(final ExecutorService es, final Duration time)
  {
    timeout = LdapUtils.assertNotNullArgOr(time, Duration::isNegative, "Timeout cannot be null or negative");
    executorService = LdapUtils.assertNotNullArg(es, "Executor service cannot be null");
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
    final List<Future<T>> futures = new ArrayList<>(callables.size());
    callables.forEach(c -> futures.add(cs.submit(c)));
    final List<ExecutionException> exceptions = new ArrayList<>(callables.size());
    for (Future<T> f : futures) {
      try {
        final T result;
        if (Duration.ZERO.equals(timeout)) {
          // blocks until a result is received
          result = f.get();
        } else {
          result = f.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        consumer.accept(result);
      } catch (ExecutionException e) {
        exceptions.add(e);
      } catch (TimeoutException e) {
        f.cancel(false);
        exceptions.add(new ExecutionException(e));
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
