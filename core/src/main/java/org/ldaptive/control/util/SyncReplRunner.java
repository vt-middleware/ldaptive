/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.ldaptive.AbstractConnectionValidator;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResultReference;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.extended.SyncInfoMessage;
import org.ldaptive.transport.Transport;
import org.ldaptive.transport.netty.ConnectionFactoryTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that executes a {@link SyncReplClient} and expects to run continuously, reconnecting if the server is
 * unavailable. Consumers must be registered to handle entries, results, and messages as they are returned from the
 * server. If the connection validator fails, the runner will be stopped and started, then the sync repl search
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

  /** Sync repl search request. */
  private final SearchRequest searchRequest;

  /** Sync repl cookie manager. */
  private final CookieManager cookieManager;

  /** Search operation handle. */
  private SyncReplClient syncReplClient;

  /** Invoked when {@link #start()} begins. */
  private Supplier<Boolean> onStart;

  /** Invoked when an entry is received. */
  private Consumer<LdapEntry> onEntry;

  /** Invoked when a reference is received. */
  private Consumer<SearchResultReference> onReference;

  /** Invoked when a result is received. */
  private Consumer<Result> onResult;

  /** Invoked when a sync info message is received. */
  private Consumer<SyncInfoMessage> onMessage;

  /** Invoked when an exception occurs. */
  private Consumer<Exception> onException;

  /** Whether the sync repl search is running. */
  private boolean started;


  /**
   * Creates a new sync repl runner. The supplied connection factory is modified to invoke {@link
   * SyncReplClient#send(SearchRequest, CookieManager)} when the connection opens and {@link SyncReplClient#cancel()}
   * when the connection closes.
   *
   * @param  cf  to get a connection from
   * @param  request  sync repl search request
   * @param  manager  sync repl cookie manager
   */
  public SyncReplRunner(final SingleConnectionFactory cf, final SearchRequest request, final CookieManager manager)
  {
    syncReplClient = new SyncReplClient(cf, true);
    searchRequest = request;
    cookieManager = manager;
    cf.setOnOpen(conn -> {
      try {
        syncReplClient.send(searchRequest, cookieManager);
      } catch (LdapException e) {
        LOGGER.error("Could not send sync repl request", e);
        return false;
      }
      return true;
    });
    cf.setOnClose(conn -> {
      try {
        if (!syncReplClient.isComplete()) {
          syncReplClient.cancel();
        }
      } catch (Exception e) {
        LOGGER.warn("Could not cancel sync repl request", e);
        return false;
      }
      return true;
    });
  }


  /**
   * Creates a new single connection factory. Uses a {@link SearchConnectionValidator} for connection validation. See
   * {@link #createTransport()}
   *
   * @param  config  sync repl connection configuration
   *
   * @return  single connection factory for use with a sync repl runner
   */
  public static SingleConnectionFactory createConnectionFactory(final ConnectionConfig config)
  {
    // CheckStyle:MagicNumber OFF
    return createConnectionFactory(
      config,
      SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .timeout(Duration.ofSeconds(5))
        .timeoutIsFailure(false)
        .build());
    // CheckStyle:MagicNumber ON
  }


  /**
   * Creates a new single connection factory. See {@link #createTransport()}.
   *
   * @param  config  sync repl connection configuration
   * @param  validator  connection validator
   *
   * @return  single connection factory for use with a sync repl runner
   */
  public static SingleConnectionFactory createConnectionFactory(
    final ConnectionConfig config, final ConnectionValidator validator)
  {
    return createConnectionFactory(createTransport(), config, validator);
  }


  /**
   * Creates a new single connection factory.
   *
   * @param  transport  sync repl connection transport
   * @param  config  sync repl connection configuration
   * @param  validator  connection validator
   *
   * @return  single connection factory for use with a sync repl runner
   */
  public static SingleConnectionFactory createConnectionFactory(
    final Transport transport, final ConnectionConfig config, final ConnectionValidator validator)
  {
    final SingleConnectionFactory factory = new SingleConnectionFactory(config, transport);
    factory.setValidator(validator);
    configureConnectionFactory(factory);
    return factory;
  }


  /**
   * Configures the supplied factory for use with a {@link SyncReplRunner}. The factory's configuration will have the
   * following modifications:
   * <ul>
   *   <li>{@link ConnectionConfig#setTransportOption(String, Object)} of AUTO_READ to false</li>
   *   <li>{@link ConnectionConfig#setAutoReconnect(boolean)} to false</li>
   *   <li>{@link ConnectionConfig#setAutoReplay(boolean)} to false</li>
   *   <li>{@link SingleConnectionFactory#setFailFastInitialize(boolean)} to false</li>
   *   <li>{@link SingleConnectionFactory#setNonBlockingInitialize(boolean)} to false</li>
   *   <li>{@link AbstractConnectionValidator#setOnFailure(Consumer)} to
   *   {@link SingleConnectionFactory.ReinitializeConnectionConsumer}</li>
   * </ul>
   *
   * @param  factory  to configure
   */
  public static void configureConnectionFactory(final SingleConnectionFactory factory)
  {
    final ConnectionConfig newConfig = ConnectionConfig.copy(factory.getConnectionConfig());
    newConfig.setTransportOption("AUTO_READ", false);
    newConfig.setAutoReconnect(false);
    newConfig.setAutoReplay(false);
    factory.setConnectionConfig(newConfig);
    factory.setFailFastInitialize(false);
    factory.setNonBlockingInitialize(false);
    if (factory.getValidator() instanceof AbstractConnectionValidator) {
      final AbstractConnectionValidator validator = (AbstractConnectionValidator) factory.getValidator();
      if (validator.getOnFailure() == null) {
        validator.setOnFailure(factory.new ReinitializeConnectionConsumer());
      }
    }
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
   * Sets the onReference consumer.
   *
   * @param  consumer  to invoke when a reference is received
   */
  public void setOnReference(final Consumer<SearchResultReference> consumer)
  {
    onReference = consumer;
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
   * Sets the onException consumer.
   *
   * @param  consumer  to invoke when an exception is received
   */
  public void setOnException(final Consumer<Exception> consumer)
  {
    onException = consumer;
  }


  /**
   * Prepare this runner for use.
   */
  public synchronized void initialize()
  {
    if (started) {
      throw new IllegalStateException("Runner has already been started");
    }
    syncReplClient.setOnEntry(onEntry);
    syncReplClient.setOnReference(onReference);
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
      // the connection factory may be shared between multiple runners
      if (!((SingleConnectionFactory) syncReplClient.getConnectionFactory()).isInitialized()) {
        ((SingleConnectionFactory) syncReplClient.getConnectionFactory()).initialize();
      }
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
      throw new IllegalStateException("Runner has not been started");
    }
    LOGGER.debug("Stopping runner {}", this);
    if (syncReplClient != null) {
      syncReplClient.close();
    }
    started = false;
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
      throw new IllegalStateException("Cannot restart the search, runner is stopped");
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
      "onReference=" + onReference + ", " +
      "onResult=" + onResult + ", " +
      "onMessage=" + onMessage + ", " +
      "onException=" + onException + ", " +
      "started=" + started;
  }
}
