/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.InitialRetryMetadata;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.SearchRequest;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.extended.SyncInfoMessage;
import org.ldaptive.transport.Transport;
import org.ldaptive.transport.netty.ConnectionFactoryTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that executes a {@link SyncReplClient} and expects to run continuously, reconnecting if the server is
 * unavailable. Consumers must be registered to handle entries, results, and messages as they are returned from the
 * server. If a consumer throws an exception, the runner will be stopped and started, then the sync repl search
 * will execute again. Consumers cannot execute blocking LDAP operations on the same connection because the next
 * incoming message is not read until the consumer has completed.
 *
 * @author  Middleware Services
 */
public class SyncReplRunner
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(SyncReplRunner.class);

  /** Number of I/O worker threads. */
  private static final int IO_WORKER_THREADS = 1;

  /** Number of message worker threads. */
  private static final int MESSAGE_WORKER_THREADS = 4;

  /** Connection transport. */
  private final Transport connectionTransport;

  /** Connection configuration. */
  private final ConnectionConfig connectionConfig;

  /** Sync repl search request. */
  private final SearchRequest searchRequest;

  /** Sync repl cookie manager. */
  private final CookieManager cookieManager;

  /** Invoked when an exception is received. */
  private final Consumer<Exception> onException = new Consumer<>() {
    @Override
    public void accept(final Exception e)
    {
      if (started) {
        if (handlingException.compareAndSet(false, true)) {
          try {
            LOGGER.warn("Received exception '{}' for {}", e.getMessage(), this);
            stop();
            start();
          } finally {
            handlingException.set(false);
          }
        } else {
          LOGGER.debug("Ignoring exception, restart already in progress for {}", this);
        }
      } else {
        LOGGER.debug("Ignoring exception, runner not started for {}", this);
      }
    }
  };

  /** Search operation handle. */
  private SyncReplClient syncReplClient;

  /** Invoked when {@link #start()} begins. */
  private Supplier<Boolean> onStart;

  /** Invoked when an entry is received. */
  private Consumer<LdapEntry> onEntry;

  /** Invoked when a result is received. */
  private Consumer<Result> onResult;

  /** Invoked when a sync info message is received. */
  private Consumer<SyncInfoMessage> onMessage;

  /** Whether the sync repl search is running. */
  private boolean started;

  /** Prevent multiple invocations of onException. */
  private AtomicBoolean handlingException = new AtomicBoolean();


  /**
   * Creates a new sync repl runner. Uses a custom {@link ConnectionFactoryTransport} for processing I/O and messages.
   *
   * @param  config  sync repl connection configuration
   * @param  request  sync repl search request
   * @param  manager  sync repl cookie manager
   */
  public SyncReplRunner(final ConnectionConfig config, final SearchRequest request, final CookieManager manager)
  {
    this(createTransport(), config, request, manager);
  }


  /**
   * Creates a new sync repl runner.
   *
   * @param  transport  sync repl connection transport
   * @param  config  sync repl connection configuration
   * @param  request  sync repl search request
   * @param  manager  sync repl cookie manager
   */
  public SyncReplRunner(
    final Transport transport,
    final ConnectionConfig config,
    final SearchRequest request,
    final CookieManager manager)
  {
    connectionTransport = transport;
    connectionConfig = config;
    searchRequest = request;
    cookieManager = manager;
  }


  /**
   * Returns a transport configured to use for sync repl. Uses its own event loop groups with auto_read set to false.
   * Detects whether Epoll or KQueue transports are available, otherwise uses NIO.
   *
   * @return  transport
   */
  private static Transport createTransport()
  {
    // message thread pool size must be >2 since exceptions are reported on the messages thread pool and flow control
    // requires a thread to signal reads and pass user events
    // startTLS and connection initializers will require additional threads
    final ConnectionFactoryTransport transport = new ConnectionFactoryTransport(
      SyncReplRunner.class.getSimpleName(),
      IO_WORKER_THREADS,
      MESSAGE_WORKER_THREADS);
    transport.setShutdownOnClose(false);
    return transport;
  }


  /**
   * Sets the onStart supplier.
   *
   * @param  supplier  to invoke on start
   */
  public void setOnStart(final Supplier<Boolean> supplier)
  {
    onStart = supplier;
  }


  /**
   * Sets the onEntry consumer.
   *
   * @param  consumer  to invoke when an entry is received
   */
  public void setOnEntry(final Consumer<LdapEntry> consumer)
  {
    onEntry = consumer;
  }


  /**
   * Sets the onResult consumer.
   *
   * @param  consumer  to invoke when a result is received
   */
  public void setOnResult(final Consumer<Result> consumer)
  {
    onResult = consumer;
  }


  /**
   * Sets the onMessage consumer.
   *
   * @param  consumer  to invoke when a sync info message is received
   */
  public void setOnMessage(final Consumer<SyncInfoMessage> consumer)
  {
    onMessage = consumer;
  }


  /**
   * Prepare this runner for use.
   *
   * @param  refreshAndPersist  whether to refresh and persist or just refresh
   * @param  reconnectWait  time to wait between open attempts
   */
  public void initialize(final boolean refreshAndPersist, final Duration reconnectWait)
  {
    if (started) {
      throw new IllegalStateException("Runner has already been started");
    }
    final SingleConnectionFactory connectionFactory = reconnectFactory(
      connectionTransport,
      connectionConfig,
      reconnectWait);
    syncReplClient = new SyncReplClient(connectionFactory, refreshAndPersist);
    syncReplClient.setOnEntry(onEntry);
    syncReplClient.setOnResult(onResult);
    syncReplClient.setOnMessage(onMessage);
    syncReplClient.setOnException(onException);
  }


  /**
   * Starts this runner.
   */
  public synchronized void start()
  {
    if (started) {
      throw new IllegalStateException("Runner has already been started");
    }
    try {
      if (onStart != null && !onStart.get()) {
        throw new RuntimeException("Start aborted from " + onStart);
      }
      LOGGER.debug("Starting runner {}", this);
      ((SingleConnectionFactory) syncReplClient.getConnectionFactory()).initialize();
      syncReplClient.send(searchRequest, cookieManager);
      started = true;
      LOGGER.info("Runner {} started", this);
    } catch (Exception e) {
      LOGGER.error("Could not start the runner", e);
    }
  }


  /**
   * Stops this runner.
   */
  public synchronized void stop()
  {
    if (!started) {
      return;
    }
    LOGGER.debug("Stopping runner {}", this);
    started = false;
    if (syncReplClient != null) {
      try {
        if (!syncReplClient.isComplete()) {
          syncReplClient.cancel();
        }
      } catch (Exception e) {
        LOGGER.warn("Could not cancel sync repl request", e);
      } finally {
        syncReplClient.close();
      }
    }
    LOGGER.info("Runner {} stopped", this);
  }


  /**
   * Returns whether this runner is started.
   *
   * @return  whether this runner is started
   */
  public boolean isStarted()
  {
    return started;
  }


  /**
   * Cancels the sync repl search and sends a new search request.
   */
  public synchronized void restartSearch()
  {
    if (!started) {
      throw new IllegalStateException("Runner is stopped");
    }
    try {
      if (!syncReplClient.isComplete()) {
        syncReplClient.cancel();
      }
    } catch (Exception e) {
      LOGGER.warn("Could not cancel sync repl request", e);
    }
    try {
      syncReplClient.send(searchRequest, cookieManager);
    } catch (LdapException e) {
      throw new IllegalStateException("Could not send sync repl request", e);
    }
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" +
      "syncReplClient=" + syncReplClient + ", " +
      "searchRequest=" + searchRequest + ", " +
      "cookieManager=" + cookieManager + ", " +
      "onStart=" + onStart + ", " +
      "onEntry=" + onEntry + ", " +
      "onResult=" + onResult + ", " +
      "onMessage=" + onMessage + ", " +
      "started=" + started;
  }


  /**
   * Creates a new single connection factory that will attempt to reconnect indefinitely. This method creates a copy of
   * the supplied config makes the following modifications:
   * <ul>
   *   <li>{@link ConnectionConfig#setAutoReconnect(boolean)} to true</li>
   *   <li>{@link ConnectionConfig#setAutoReconnectCondition(Predicate)} to sleep and return true for
   *   InitialRetryMetadata</li>
   *   <li>{@link ConnectionConfig#setAutoReplay(boolean)} to false</li>
   * </ul>
   *
   * @param  transport  connection transport
   * @param  cc  connection configuration
   * @param  wait  length of time to wait between consecutive calls to open
   *
   * @return  single connection factory
   */
  protected static SingleConnectionFactory reconnectFactory(
    final Transport transport,
    final ConnectionConfig cc,
    final Duration wait)
  {
    final ConnectionConfig newConfig = ConnectionConfig.copy(cc);
    newConfig.setTransportOption("AUTO_READ", false);
    newConfig.setAutoReconnect(false);
    newConfig.setAutoReconnectCondition(metadata -> {
      if (metadata instanceof InitialRetryMetadata) {
        try {
          LOGGER.debug("Waiting {}ms to reconnect", wait.toMillis());
          Thread.sleep(wait.toMillis());
        } catch (InterruptedException ignored) {}
        return true;
      }
      return false;
    });
    newConfig.setAutoReplay(false);
    final SingleConnectionFactory factory = new SingleConnectionFactory(newConfig, transport);
    factory.setFailFastInitialize(true);
    factory.setNonBlockingInitialize(false);
    return factory;
  }
}
