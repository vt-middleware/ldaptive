/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.InitialRetryMetadata;
import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.extended.SyncInfoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that executes a {@link SyncReplClient} and expects to run continuously, reconnecting if the server is
 * unavailable. Consumers must be registered to handle entries, results, and messages as they are returned from the
 * server. If a consumer throws an exception, the connection will the closed and reopened, then the sync repl search
 * will execute again.
 *
 * @author  Middleware Services
 */
public class SyncReplRunner
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection configuration. */
  private final ConnectionConfig connectionConfig;

  /** Sync repl search request. */
  private final SearchRequest searchRequest;

  /** Sync repl cookie manager. */
  private final CookieManager cookieManager;

  /** Size of the sync repl result queue. */
  private final int queueSize;

  /** Search operation handle. */
  private SyncReplClient syncReplClient;

  /** Invoked when {@link #start()} begins. */
  private Supplier<Boolean> onStart;

  /** Invoked when an entry is received. */
  private Consumer<SyncReplItem.Entry> onEntry;

  /** Invoked when a result is received. */
  private Consumer<SyncReplItem.Result> onResult;

  /** Invoked when a sync info message is received. */
  private Consumer<SyncInfoMessage> onMessage;

  /** Whether {@link #start()} has been invoked. */
  private boolean run;

  /** Whether {@link #stop()} has been invoked. */
  private boolean stop;


  /**
   * Creates a new sync repl runner.
   *
   * @param  config  sync repl connection configuration
   * @param  request  sync repl search request
   * @param  manager  sync repl cookie manager
   * @param  size  sync repl result queue size
   */
  public SyncReplRunner(
    final ConnectionConfig config,
    final SearchRequest request,
    final CookieManager manager,
    final int size)
  {
    connectionConfig = config;
    searchRequest = request;
    cookieManager = manager;
    queueSize = size;
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
  public void setOnEntry(final Consumer<SyncReplItem.Entry> consumer)
  {
    onEntry = consumer;
  }


  /**
   * Sets the onResult consumer.
   *
   * @param  consumer  to invoke when a result is received
   */
  public void setOnResult(final Consumer<SyncReplItem.Result> consumer)
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
    if (run) {
      throw new IllegalStateException("Runner has already been started");
    }
    final SingleConnectionFactory connectionFactory = reconnectFactory(connectionConfig, reconnectWait);
    syncReplClient = new SyncReplClient(connectionFactory, refreshAndPersist);
    stop = false;
  }


  /**
   * Starts this runner.
   */
  public synchronized void start()
  {
    if (run) {
      throw new IllegalStateException("Runner has already been started");
    }
    run = true;
    try {
      if (onStart != null && !onStart.get()) {
        throw new RuntimeException("Start aborted from " + onStart);
      }
      BlockingQueue<SyncReplItem> results = null;
      logger.info("Runner {} started", this);
      while (run) {
        try {
          if (!((SingleConnectionFactory) syncReplClient.getConnectionFactory()).isInitialized()) {
            ((SingleConnectionFactory) syncReplClient.getConnectionFactory()).initialize();
            results = syncReplClient.execute(searchRequest, cookieManager, queueSize);
          }
          if (results == null) {
            logger.error("Blocking queue has not been initialized");
            break;
          }
          // blocks until result is received
          final SyncReplItem item = results.take();
          logger.debug("Received item {}", item);
          if (item.isEntry() && onEntry != null) {
            onEntry.accept(item.getEntry());
          } else if (item.isResult() && onResult != null) {
            onResult.accept(item.getResult());
          } else if (item.isMessage() && onMessage != null) {
            onMessage.accept(item.getMessage());
          } else if (item.isException()) {
            throw item.getException();
          }
        } catch (Exception e) {
          if (run) {
            logger.error("Unexpected error, closing the connection factory. Runner will reconnect.", e);
            try {
              syncReplClient.cancel();
            } catch (LdapException le) {
              logger.warn("Error cancelling sync repl operation");
            }
            syncReplClient.getConnectionFactory().close();
          } else {
            logger.error("Unexpected error. Runner will exit.", e);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Could not start the runner", e);
    } finally {
      stop();
    }
  }


  /**
   * Stops this runner.
   */
  public synchronized void stop()
  {
    if (stop) {
      return;
    }
    stop = true;
    run = false;
    if (syncReplClient != null) {
      try {
        syncReplClient.cancel();
      } catch (Exception e) {
        logger.warn("Could not cancel sync repl request", e);
      } finally {
        syncReplClient.close();
      }
    }
    logger.info("Runner {} stopped", this);
  }


  @Override
  public String toString()
  {
    return new StringBuilder().append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("syncReplClient=").append(syncReplClient).append(", ")
      .append("searchRequest=").append(searchRequest).append(", ")
      .append("cookieManager=").append(cookieManager).append(", ")
      .append("queueSize=").append(queueSize).append(", ")
      .append("onStart=").append(onStart).append(", ")
      .append("onEntry=").append(onEntry).append(", ")
      .append("onResult=").append(onResult).append(", ")
      .append("onMessage=").append(onMessage).append(", ")
      .append("run=").append(run).toString();
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
   * @param  cc  connection configuration
   * @param  wait  length of time to wait between consecutive calls to open
   *
   * @return  single connection factory
   */
  protected static SingleConnectionFactory reconnectFactory(final ConnectionConfig cc, final Duration wait)
  {
    final ConnectionConfig newConfig = ConnectionConfig.copy(cc);
    newConfig.setAutoReconnect(true);
    newConfig.setAutoReconnectCondition(metadata -> {
      if (metadata instanceof InitialRetryMetadata) {
        try {
          Thread.sleep(wait.toMillis());
        } catch (InterruptedException e) {}
        return true;
      }
      return false;
    });
    newConfig.setAutoReplay(false);
    final SingleConnectionFactory factory = new SingleConnectionFactory(newConfig);
    factory.setFailFastInitialize(true);
    factory.setNonBlockingInitialize(false);
    return factory;
  }
}
