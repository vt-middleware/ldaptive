/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.ldaptive.transport.ThreadPoolConfig;
import org.ldaptive.transport.TransportFactory;
import org.ldaptive.transport.netty.SimpleNettyServer;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SingleConnectionFactory}.
 *
 * @author  Middleware Services
 */
public class SingleConnectionFactoryTest
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
      final SingleConnectionFactory factory = new SingleConnectionFactory(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build(),
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .ioThreads(1)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()));
      try {
        factory.initialize();
        try (Connection conn = factory.getConnection()) {
          assertThat(conn).isNotNull();
          assertThat(conn.isOpen()).isTrue();
        }
      } finally {
        factory.close();
        try {
          factory.getConnection();
          fail("Should have thrown exception");
        } catch (Exception e) {
          assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
        }
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
      final SingleConnectionFactory factory = new SingleConnectionFactory(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build(),
        TransportFactory.getTransport(
          ThreadPoolConfig.builder()
            .ioThreads(1)
            .messageThreads(1)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .build()));
      try {
        factory.initialize();
        try (Connection conn = factory.getConnection()) {
          assertThat(conn).isNotNull();
          assertThat(conn.isOpen()).isTrue();
        }
      } finally {
        factory.close();
        try {
          factory.getConnection();
          fail("Should have thrown exception");
        } catch (Exception e) {
          assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
        }
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
      final SingleConnectionFactory factory = new SingleConnectionFactory(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .autoReconnect(true)
          .autoReconnectCondition(metadata -> {
            if (count.get() > 0) {
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
      try {
        factory.initialize();
        final SearchRequest request = SearchRequest.builder().filter("(objectClass=*)").build();
        final SearchOperationHandle handle = new SearchOperation(factory).send(request);
        try {
          handle.await();
          fail("Should have thrown exception");
        } catch (Exception e) {
          assertThat(e).isExactlyInstanceOf(LdapException.class);
        }
        try {
          final Connection conn = factory.getConnection();
          assertThat(conn).isNotNull();
          assertThat(conn.isOpen()).isTrue();
        } catch (Exception e) {
          fail("Should not have thrown exception");
        }
      } finally {
        factory.close();
        try {
          factory.getConnection();
          fail("Should have thrown exception");
        } catch (Exception e) {
          assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
        }
        assertThat(count.get()).isEqualTo(1);
      }
    } finally {
      server.stop();
    }
  }


  @Test(groups = "netty")
  public void immutable()
  {
    final SingleConnectionFactory factory = new SingleConnectionFactory();
    factory.setValidator(new SearchConnectionValidator());

    factory.assertMutable();
    ((Freezable) factory.getValidator()).assertMutable();

    factory.freeze();
    TestUtils.testImmutable(factory);
    TestUtils.testImmutable((Freezable) factory.getValidator());
  }
}
