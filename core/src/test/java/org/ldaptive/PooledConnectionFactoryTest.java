/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.ldaptive.pool.BindConnectionPassivator;
import org.ldaptive.pool.IdlePruneStrategy;
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
      factory.setName("pooled-connection-factory-test-open-and-close");
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
      factory.setName("pooled-connection-factory-test-open-and-close");
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
      factory.setName("pooled-connection-factory-test-open-and-reconnect");
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
    final SimpleNettyServer server = new SimpleNettyServer();
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
          .build())
        .build();
      factory.setName("pooled-connection-factory-test-validate-periodically");
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
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test-validate-periodically-restart")
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .validator(SearchConnectionValidator.builder()
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
    final SimpleNettyServer server = new SimpleNettyServer(
      null,
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          try {
            Thread.sleep(serverWait.get().toMillis());
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          ctx.channel().writeAndFlush(SearchResponse.builder()
            .messageID(1)
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
        .name("pooled-connection-factory-test-validate-periodically-with-timeout")
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
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final AtomicInteger validateCount = new AtomicInteger();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder(
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()))
        .name("pooled-connection-factory-test-validate-one-checkout")
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
  public void pruneBelowMin()
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
        .name("pooled-connection-factory-test-prune-below-min")
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
        final Connection c1 = factory.getConnection();
        c1.close();
        Thread.sleep(50);
        factory.prune();
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
        .name("pooled-connection-factory-test-prune-above-min")
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
        final Connection c1 = factory.getConnection();
        final Connection c2 = factory.getConnection();
        final Connection c3 = factory.getConnection();
        c1.close();
        c2.close();
        c3.close();
        assertThat(factory.availableCount()).isEqualTo(3);
        Thread.sleep(50);
        factory.prune();
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
  public void pruneAllActive()
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
        .name("pooled-connection-factory-test-prune-all-active")
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
        final Connection c1 = factory.getConnection();
        final Connection c2 = factory.getConnection();
        final Connection c3 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        Thread.sleep(50);
        factory.prune();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(3);
        c1.close();
        c2.close();
        c3.close();
        assertThat(factory.availableCount()).isEqualTo(3);
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
        .name("pooled-connection-factory-test-prune-active-above-min")
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
        final Connection c1 = factory.getConnection();
        final Connection c2 = factory.getConnection();
        final Connection c3 = factory.getConnection();
        final Connection c4 = factory.getConnection();
        final Connection c5 = factory.getConnection();
        assertThat(factory.availableCount()).isEqualTo(0);
        assertThat(factory.activeCount()).isEqualTo(5);
        c1.close();
        c2.close();
        assertThat(factory.availableCount()).isEqualTo(2);
        assertThat(factory.activeCount()).isEqualTo(3);
        Thread.sleep(50);
        factory.prune();
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
