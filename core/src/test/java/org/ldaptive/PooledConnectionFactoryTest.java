/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import io.netty.channel.Channel;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.ldaptive.pool.BindConnectionPassivator;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolException;
import org.ldaptive.pool.PooledConnectionProxy;
import org.ldaptive.transport.ThreadPoolConfig;
import org.ldaptive.transport.TransportFactory;
import org.ldaptive.transport.netty.SimpleNettyServer;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public class PooledConnectionFactoryTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void openAndClose()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = new PooledConnectionFactory(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build(),
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()));
      factory.setName("pooled-connection-factory-test");
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(3);
      } catch (Exception e) {
        fail("Should not have thrown an exception", e);
      } finally {
        factory.close();
        assertThat(factory.availableCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void openAndCloseMessageThreadPool()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = new PooledConnectionFactory(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build(),
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .ioThreads(1)
            .messageThreads(1)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()));
      factory.setName("pooled-connection-factory-test");
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(3);
      } finally {
        factory.close();
        assertThat(factory.availableCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void openAndReconnect()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          ctx.fireUserEventTriggered(SimpleNettyServer.Event.DISCONNECT);
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final AtomicInteger count = new AtomicInteger();
      final PooledConnectionFactory factory = new PooledConnectionFactory(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .autoReconnect(true)
          .autoReconnectCondition(metadata -> {
            if (count.get() >= 10) {
              return false;
            }
            count.incrementAndGet();
            try {
              Thread.sleep(100);
            } catch (InterruptedException ignored) {}
            return metadata instanceof ClosedRetryMetadata && metadata.getAttempts() == 0;
          })
          .build(),
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .threadPoolName(getClass().getSimpleName() + "-io")
            .ioThreads(1)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()));
      factory.setName("pooled-connection-factory-test");
      factory.setMinPoolSize(10);
      factory.setMaxPoolSize(10);
      try {
        factory.initialize();
        final SearchRequest request = SearchRequest.builder().filter("(objectClass=*)").build();
        final SearchOperationWorker worker = new SearchOperationWorker(new SearchOperation(factory));
        final Collection<OperationHandle<SearchRequest, SearchResponse>> responses = worker.send(
          new SearchRequest[] {
            request, request, request, request, request, request, request, request, request, request});
        for (OperationHandle<SearchRequest, SearchResponse> handle : responses) {
          try {
            handle.await();
            fail("Should have thrown exception");
          } catch (Exception e) {
            assertThat(e).isExactlyInstanceOf(LdapException.class);
          }
        }
        // allow time for reconnects to occur
        Thread.sleep(10);
        assertThat(factory.availableCount()).isEqualTo(10);
      } finally {
        if (factory.isInitialized()) {
          factory.close();
        }
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
        assertThat(count.get()).isEqualTo(10);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void validatePeriodically()
    throws Exception
  {
    final Map<Channel, AtomicInteger> msgIds = new ConcurrentHashMap<>();
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          msgIds.putIfAbsent(ctx.channel(), new AtomicInteger());
          ctx.channel().writeAndFlush(SearchResponse.builder()
            .messageID(msgIds.get(ctx.channel()).incrementAndGet())
            .resultCode(ResultCode.SUCCESS)
            .build());
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .validator(SearchConnectionValidator.builder()
          .timeout(Duration.ofSeconds(5))
          .build())
        .build();
      factory.setName("pooled-connection-factory-test");
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(3);
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(3);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void validatePeriodicallyRestart()
    throws Exception
  {
    final Map<Channel, AtomicInteger> msgIds = new ConcurrentHashMap<>();
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          msgIds.putIfAbsent(ctx.channel(), new AtomicInteger());
          ctx.channel().writeAndFlush(SearchResponse.builder()
            .messageID(msgIds.get(ctx.channel()).incrementAndGet())
            .resultCode(ResultCode.SUCCESS)
            .build());
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .validator(SearchConnectionValidator.builder()
          .timeout(Duration.ofSeconds(5))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(3);
        server.stop();
        factory.validate();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
        server.start();
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(3);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void validatePeriodicallyWithTimeout()
    throws Exception
  {
    final AtomicReference<Duration> serverWait = new AtomicReference<>(Duration.ofSeconds(30));
    final Map<Channel, AtomicInteger> msgIds = new ConcurrentHashMap<>();
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          try {
            Thread.sleep(serverWait.get().toMillis());
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          msgIds.putIfAbsent(ctx.channel(), new AtomicInteger());
          ctx.channel().writeAndFlush(SearchResponse.builder()
            .messageID(msgIds.get(ctx.channel()).incrementAndGet())
            .resultCode(ResultCode.SUCCESS)
            .build());
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(0)
        .validator(SearchConnectionValidator.builder()
          .timeout(Duration.ofSeconds(1))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(0);
        // get 2 connections to grow the pool size and then validate
        Connection c1 = factory.getConnection();
        Connection c2 = factory.getConnection();
        c1.close();
        c2.close();
        assertThat(factory.availableCount()).isEqualTo(2);
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(0);

        // change the wait time and repeat
        serverWait.set(Duration.ofMillis(500));
        c1 = factory.getConnection();
        c2 = factory.getConnection();
        c1.close();
        c2.close();
        assertThat(factory.availableCount()).isEqualTo(2);
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(2);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void validateOnCheckout()
    throws Exception
  {
    final Map<Channel, AtomicInteger> msgIds = new ConcurrentHashMap<>();
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          msgIds.putIfAbsent(ctx.channel(), new AtomicInteger());
          ctx.channel().writeAndFlush(SearchResponse.builder()
            .messageID(msgIds.get(ctx.channel()).incrementAndGet())
            .resultCode(ResultCode.SUCCESS)
            .build());
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final AtomicInteger validateCount = new AtomicInteger();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(0)
        .validator(new ConnectionValidator() {
          @Override
          public void applyAsync(final Connection conn, final Consumer<Boolean> function) {}

          @Override
          public Supplier<Boolean> applyAsync(final Connection conn)
          {
            return () -> false;
          }

          @Override
          public Duration getValidatePeriod()
          {
            return Duration.ofMinutes(5);
          }

          @Override
          public Duration getValidateTimeout()
          {
            return Duration.ofSeconds(5);
          }

          @Override
          public Boolean apply(final Connection connection)
          {
            return validateCount.getAndIncrement() != 1;
          }
        })
        .validateOnCheckOut(true)
        .build();
      factory.setValidationExceptionHandler(factory.new RetryValidationExceptionHandler());
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(0);
        assertThat(validateCount.intValue()).isEqualTo(0);
        Connection c1 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(1);
        assertThat(validateCount.intValue()).isEqualTo(1);
        c1.close();
        assertThat(factory.availableCount()).isEqualTo(1);
        assertThat(factory.activeCount()).isEqualTo(0);

        // getConnection should result in a ValidationException, which results in another call to getConnection
        c1 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(1);
        assertThat(validateCount.intValue()).isEqualTo(3);
        c1.close();
        assertThat(factory.availableCount()).isEqualTo(1);
        assertThat(factory.activeCount()).isEqualTo(0);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void validateLargePool()
    throws Exception
  {
    final Map<Channel, AtomicInteger> msgIds = new ConcurrentHashMap<>();
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          msgIds.putIfAbsent(ctx.channel(), new AtomicInteger());
          ctx.channel().writeAndFlush(SearchResponse.builder()
            .messageID(msgIds.get(ctx.channel()).incrementAndGet())
            .resultCode(ResultCode.SUCCESS)
            .build());
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
          TransportFactory.getTransport(
            ThreadPoolConfig.builder()
              .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
              .build()))
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(10)
        .max(1000)
        .validator(SearchConnectionValidator.builder()
          .timeout(Duration.ofSeconds(5))
          .build())
        .build();
      factory.setName("pooled-connection-factory-test");
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(10);
        assertThat(factory.activeCount()).isEqualTo(0);
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(10);
        assertThat(factory.activeCount()).isEqualTo(0);
        final List<Connection> conns = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
          conns.add(factory.getConnection());
        }
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(1000);
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(1000);
        conns.forEach(Connection::close);
        assertThat(factory.availableCount()).isEqualTo(1000);
        assertThat(factory.activeCount()).isEqualTo(0);
        factory.validate();
        assertThat(factory.availableCount()).isEqualTo(1000);
        assertThat(factory.activeCount()).isEqualTo(0);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void pruneNoOp()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(2)
        .max(5)
        .pruneStrategy(IdlePruneStrategy.builder()
          .idle(Duration.ofMillis(25))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        final Connection c1 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(1);
        assertThat(factory.activeCount()).isEqualTo(1);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(1);
        assertThat(factory.activeCount()).isEqualTo(1);
        c1.close();
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        // no connections should have been pruned
        assertThat(hasConnection(factory, c1)).isTrue();
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void pruneMaxAge()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
          TransportFactory.getTransport(
            ThreadPoolConfig.builder()
              .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
              .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(2)
        .max(5)
        .pruneStrategy(IdlePruneStrategy.builder()
          .idle(Duration.ofMillis(25))
          .age(Duration.ofMillis(250))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        final Connection c1 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(1);
        assertThat(factory.activeCount()).isEqualTo(1);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(1);
        assertThat(factory.activeCount()).isEqualTo(1);
        c1.close();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        // idle prune will not go below the minimum, same connection should still exist in the pool
        assertThat(hasConnection(factory, c1)).isTrue();

        waitAndPrune(factory, Duration.ofMillis(200));
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        // age prune will go below the minimum, same connection should not exist in the pool
        assertThat(hasConnection(factory, c1)).isFalse();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void pruneAboveMin()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(2)
        .max(5)
        .pruneStrategy(IdlePruneStrategy.builder()
          .idle(Duration.ofMillis(25))
          .age(Duration.ofMillis(250))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        final Connection c1 = factory.getConnection();
        final Connection c2 = factory.getConnection();
        final Connection c3 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        c1.close();
        c2.close();
        c3.close();
        assertThat(factory.availableCount()).isEqualTo(3);
        assertThat(factory.activeCount()).isEqualTo(0);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        // idle prune will not go below the minimum, two of the same connections should still exist in the pool
        int count = 0;
        if (hasConnection(factory, c1)) {
          count++;
        }
        if (hasConnection(factory, c2)) {
          count++;
        }
        if (hasConnection(factory, c3)) {
          count++;
        }
        assertThat(count).isEqualTo(2);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void pruneAllActiveDuringPrune()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(2)
        .max(5)
        .pruneStrategy(IdlePruneStrategy.builder()
          .idle(Duration.ofMillis(25))
          .age(Duration.ofMillis(250))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        final Connection c1 = factory.getConnection();
        final Connection c2 = factory.getConnection();
        final Connection c3 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        c1.close();
        c2.close();
        c3.close();
        assertThat(factory.availableCount()).isEqualTo(3);
        assertThat(factory.activeCount()).isEqualTo(0);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void pruneActiveAboveMin()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(2)
        .max(5)
        .pruneStrategy(IdlePruneStrategy.builder()
          .idle(Duration.ofMillis(25))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(0);
        final Connection c1 = factory.getConnection();
        final Connection c2 = factory.getConnection();
        final Connection c3 = factory.getConnection();
        final Connection c4 = factory.getConnection();
        final Connection c5 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(5);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(5);
        c1.close();
        c2.close();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(3);
        // prune will remove idle connections while other connections are active
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        c3.close();
        c4.close();
        c5.close();
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void pruneLargePool()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
          TransportFactory.getTransport(
            ThreadPoolConfig.builder()
              .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
              .build()))
        .name("pooled-connection-factory-test")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .min(10)
        .max(1000)
        .pruneStrategy(IdlePruneStrategy.builder()
          .idle(Duration.ofMillis(25))
          .build())
        .build();
      try {
        factory.initialize();
        assertThat(factory.availableCount()).isEqualTo(10);
        assertThat(factory.activeCount()).isEqualTo(0);
        final List<Connection> conns = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
          conns.add(factory.getConnection());
        }
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(1000);
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(1000);
        conns.forEach(Connection::close);
        assertThat(factory.availableCount()).isEqualTo(1000);
        assertThat(factory.activeCount()).isEqualTo(0);
        // prune will remove idle connections down to min size
        waitAndPrune(factory, Duration.ofMillis(50));
        assertThat(factory.availableCount()).isEqualTo(10);
        assertThat(factory.activeCount()).isEqualTo(0);
      } finally {
        factory.close();
        assertThat(factory.availableCount() + factory.activeCount()).isEqualTo(0);
      }
    } finally {
      server.stop();
    }
  }


  /**
   * Waits the supplied time and then invokes {@link PooledConnectionFactory#prune()}.
   *
   * @param  factory  to prune
   * @param  wait  before pruning
   *
   * @throws  InterruptedException  if waiting is interrupted
   */
  private static void waitAndPrune(final PooledConnectionFactory factory, final Duration wait)
    throws InterruptedException
  {
    Thread.sleep(wait.toMillis());
    factory.prune();
  }


  /**
   * Returns whether the supplied factory contains the supplied connection.
   *
   * @param  factory  to inspect
   * @param  conn  connection to test for
   *
   * @return  whether factory contains connection
   */
  private static boolean hasConnection(final PooledConnectionFactory factory, final Connection conn)
  {
    final List<Connection> available = new ArrayList<>(factory.availableCount());
    try {
      while (factory.availableCount() > 0) {
        try {
          available.add(factory.getConnection());
        } catch (PoolException e) {
          throw new RuntimeException(e);
        }
      }
      return available.stream()
        .anyMatch(
          c -> ((PooledConnectionProxy) Proxy.getInvocationHandler(conn)).getConnection().equals(
            ((PooledConnectionProxy) Proxy.getInvocationHandler(c)).getConnection()));
    } finally {
      available.forEach(Connection::close);
    }
  }


  @Test(groups = "netty")
  public void immutable()
  {
    final PooledConnectionFactory factory = new PooledConnectionFactory();
    factory.setPassivator(new BindConnectionPassivator());
    factory.setValidator(new SearchConnectionValidator());
    factory.setPruneStrategy(new IdlePruneStrategy());
    factory.setDefaultConnectionFactory(new DefaultConnectionFactory());

    factory.assertMutable();
    ((Freezable) factory.getPassivator()).assertMutable();
    ((Freezable) factory.getValidator()).assertMutable();
    ((Freezable) factory.getPruneStrategy()).assertMutable();
    factory.getDefaultConnectionFactory().assertMutable();

    factory.freeze();
    TestUtils.testImmutable(factory);
    TestUtils.testImmutable((Freezable) factory.getPassivator());
    TestUtils.testImmutable((Freezable) factory.getValidator());
    TestUtils.testImmutable((Freezable) factory.getPruneStrategy());
    TestUtils.testImmutable(factory.getDefaultConnectionFactory());
  }
}
