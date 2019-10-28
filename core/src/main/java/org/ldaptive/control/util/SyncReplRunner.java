/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.InitialRetryMetadata;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.SearchRequest;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.extended.SyncInfoMessage;
import org.ldaptive.transport.Transport;
import org.ldaptive.transport.netty.NettyTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that executes a {@link SyncReplClient} and expects to run continuously, reconnecting if the server is
 * unavailable. Consumers must be registered to handle entries, results, and messages as they are returned from the
 * server. If a consumer throws an exception, the runner will be stopped and started, then the sync repl search
 * will execute again.
 *
 * @author  Middleware Services
 */
public class SyncReplRunner
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection transport. */
  private final Transport connectionTransport;

  /** Connection configuration. */
  private final ConnectionConfig connectionConfig;

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

  /** Invoked when a result is received. */
  private Consumer<Result> onResult;

  /** Invoked when a sync info message is received. */
  private Consumer<SyncInfoMessage> onMessage;

  /** Whether {@link #start()} has been invoked. */
  private boolean started;

  /** Whether {@link #stop()} has been invoked. */
  private boolean stopped;


  /**
   * Creates a new sync repl runner. Uses a {@link NettyTransport} with a single thread {@link DefaultEventLoopGroup}
   * for processing inbound messages.
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
   * Returns a transport configured to use for sync repl. Uses it's own event loop groups with auto_read set to false.
   * Detects whether Epoll or KQueue transports are available, otherwise uses NIO.
   *
   * @return  transport
   */
  private static Transport createTransport()
  {
    final NettyTransport transport;
    if (Epoll.isAvailable()) {
      transport = new NettyTransport(
        EpollSocketChannel.class,
        new EpollEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory("syncReplRunner-io", true, Thread.NORM_PRIORITY))),
        new DefaultEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory("syncReplRunner-messages", true, Thread.NORM_PRIORITY))),
        Collections.singletonMap(ChannelOption.AUTO_READ, false));
    } else if (KQueue.isAvailable()) {
      transport = new NettyTransport(
        KQueueSocketChannel.class,
        new KQueueEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory("syncReplRunner-io", true, Thread.NORM_PRIORITY))),
        new DefaultEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory("syncReplRunner-messages", true, Thread.NORM_PRIORITY))),
        Collections.singletonMap(ChannelOption.AUTO_READ, false));
    } else {
      transport = new NettyTransport(
        NioSocketChannel.class,
        new NioEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory("syncReplRunner-io", true, Thread.NORM_PRIORITY))),
        new DefaultEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory("syncReplRunner-messages", true, Thread.NORM_PRIORITY))),
        Collections.singletonMap(ChannelOption.AUTO_READ, false));
    }
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
    syncReplClient.setOnException(e -> {
      logger.warn("Received exception '{}' with started={}", e.getMessage(), started);
      if (started) {
        stop();
        start();
      }
    });
    stopped = false;
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
      ((SingleConnectionFactory) syncReplClient.getConnectionFactory()).initialize();
      syncReplClient.send(searchRequest, cookieManager);
      started = true;
      logger.info("Runner {} started", this);
    } catch (Exception e) {
      logger.error("Could not start the runner", e);
    }
  }


  /**
   * Stops this runner.
   */
  public synchronized void stop()
  {
    if (stopped) {
      return;
    }
    stopped = true;
    started = false;
    if (syncReplClient != null) {
      try {
        if (!syncReplClient.isComplete()) {
          syncReplClient.cancel();
        }
      } catch (Exception e) {
        logger.warn("Could not cancel sync repl request", e);
      } finally {
        syncReplClient.close();
      }
    }
    logger.info("Runner {} stopped", this);
  }


  /**
   * Cancels the sync repl search and sends a new search request.
   */
  public synchronized void restartSearch()
  {
    if (stopped) {
      throw new IllegalStateException("Runner is stopped");
    }
    try {
      if (!syncReplClient.isComplete()) {
        syncReplClient.cancel();
      }
    } catch (Exception e) {
      logger.warn("Could not cancel sync repl request", e);
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
    return new StringBuilder().append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("syncReplClient=").append(syncReplClient).append(", ")
      .append("searchRequest=").append(searchRequest).append(", ")
      .append("cookieManager=").append(cookieManager).append(", ")
      .append("onStart=").append(onStart).append(", ")
      .append("onEntry=").append(onEntry).append(", ")
      .append("onResult=").append(onResult).append(", ")
      .append("onMessage=").append(onMessage).append(", ")
      .append("started=").append(started).toString();
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
    newConfig.setAutoReconnect(true);
    newConfig.setAutoReconnectCondition(metadata -> {
      if (metadata instanceof InitialRetryMetadata) {
        try {
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
