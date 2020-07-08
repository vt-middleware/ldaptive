/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
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
      }
    } finally {
      server.stop();
    }
  }
}
