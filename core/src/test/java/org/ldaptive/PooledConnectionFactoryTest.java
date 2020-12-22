/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.ldaptive.transport.netty.NettyConnectionFactoryTransport;
import org.ldaptive.transport.netty.SimpleNettyServer;
import org.testng.Assert;
import org.testng.annotations.Test;

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
          .build());
      try {
        factory.initialize();
      } finally {
        factory.close();
        Assert.assertEquals(factory.availableCount(), 0);
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
        new NettyConnectionFactoryTransport(
          NioSocketChannel.class,
          new NioEventLoopGroup(
            1,
            new ThreadPerTaskExecutor(
              new DefaultThreadFactory(getClass().getSimpleName() + "-io", true, Thread.NORM_PRIORITY))),
          null));
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
            Assert.fail("Should have thrown exception");
          } catch (Exception e) {
            Assert.assertEquals(e.getClass(), LdapException.class);
          }
        }
      } finally {
        if (factory.isInitialized()) {
          factory.close();
        }
        Assert.assertEquals(factory.availableCount() + factory.activeCount(), 0);
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
      final PooledConnectionFactory factory = PooledConnectionFactory.builder()
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .validator(SearchConnectionValidator.builder()
          .build())
        .build();
      try {
        factory.initialize();
        Assert.assertEquals(factory.availableCount(), 3);
        factory.validate();
        Assert.assertEquals(factory.availableCount(), 3);
      } finally {
        factory.close();
        Assert.assertEquals(factory.availableCount() + factory.activeCount(), 0);
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
      final PooledConnectionFactory factory = PooledConnectionFactory.builder()
        .config(ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build())
        .validator(SearchConnectionValidator.builder()
          .build())
        .build();
      try {
        factory.initialize();
        Assert.assertEquals(factory.availableCount(), 3);
        server.stop();
        factory.validate();
        Assert.assertEquals(factory.availableCount() + factory.activeCount(), 0);
        server.start();
        factory.validate();
        Assert.assertEquals(factory.availableCount(), 3);
      } finally {
        factory.close();
        Assert.assertEquals(factory.availableCount() + factory.activeCount(), 0);
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
      final PooledConnectionFactory factory = PooledConnectionFactory.builder()
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
        Assert.assertEquals(factory.availableCount(), 0);
        // get 2 connections to grow the pool size and then validate
        Connection c1 = factory.getConnection();
        Connection c2 = factory.getConnection();
        c1.close();
        c2.close();
        Assert.assertEquals(factory.availableCount(), 2);
        factory.validate();
        Assert.assertEquals(factory.availableCount(), 0);

        // change the wait time and repeat
        serverWait.set(Duration.ofMillis(500));
        c1 = factory.getConnection();
        c2 = factory.getConnection();
        c1.close();
        c2.close();
        Assert.assertEquals(factory.availableCount(), 2);
        factory.validate();
        Assert.assertEquals(factory.availableCount(), 2);
      } finally {
        factory.close();
        Assert.assertEquals(factory.availableCount() + factory.activeCount(), 0);
      }
    } finally {
      server.stop();
    }
  }
}
