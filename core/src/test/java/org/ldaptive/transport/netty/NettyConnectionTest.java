/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.ldaptive.ClosedRetryMetadata;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionValidator;
import org.ldaptive.LdapURL;
import org.ldaptive.SearchScope;
import org.ldaptive.UnbindRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link NettyConnection}.
 *
 * @author  Middleware Services
 */
public class NettyConnectionTest
{


  /**
   * Returns thread pools of size 1, 2 and default.
   *
   * @return  thread pools
   */
  @DataProvider(name = "threadPools")
  public Object[][] createThreadPools()
  {
    return
      new Object[][] {
        new Object[] {
          new NioEventLoopGroup(
            0,
            new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        },
        new Object[] {
          new NioEventLoopGroup(
            1,
            new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        },
        new Object[] {
          new NioEventLoopGroup(
            2,
            new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        },
      };
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void messageID()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final NettyConnection conn = new NettyConnection(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build(),
        NioSocketChannel.class,
        new NioEventLoopGroup(
          0,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        true);
      try {
        conn.open();
        assertThat(conn.isOpen()).isTrue();
        int id = conn.getMessageID();
        assertThat(id).isEqualTo(1);
        conn.operation(new UnbindRequest());
        assertThat(conn.getMessageID()).isEqualTo(id + 1);
        conn.setMessageID(Integer.MAX_VALUE - 1);
        conn.operation(new UnbindRequest());
        id = conn.getMessageID();
        assertThat(id).isEqualTo(Integer.MAX_VALUE);
        conn.operation(new UnbindRequest());
        id = conn.getMessageID();
        assertThat(id).isEqualTo(1);
        conn.operation(new UnbindRequest());
        assertThat(conn.getMessageID()).isEqualTo(id + 1);
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void transportOptions()
    throws Exception
  {
    final NettyConnection conn = new NettyConnection(
      ConnectionConfig.builder()
        .url("ldap://localhost:10389")
        .transportOption("AUTO_READ", "false")
        .transportOption("TCP_NODELAY", true)
        .transportOption("SO_SNDBUF", "1024")
        .transportOption("SO_RCVBUF", 1024)
        .build(),
      NioSocketChannel.class,
      new NioEventLoopGroup(
        0,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
      null,
      true);
    final Map<ChannelOption, Object> options = conn.getChannelOptions();
    assertThat(options).isNotNull();
    assertThat(options.get(ChannelOption.AUTO_READ)).isEqualTo(false);
    assertThat(options.get(ChannelOption.TCP_NODELAY)).isEqualTo(true);
    assertThat(options.get(ChannelOption.SO_SNDBUF)).isEqualTo(1024);
    assertThat(options.get(ChannelOption.SO_RCVBUF)).isEqualTo(1024);
  }


  @Test(groups = "netty")
  public void getLdapURL()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final NettyConnection conn = new NettyConnection(
        ConnectionConfig.builder()
          .url(
            "ldap://" + address.getHostName() + ":" + address.getPort() + "/dc=ldaptive,dc=org?cn,sn?one?(uid=dfisher)")
          .build(),
        NioSocketChannel.class,
        new NioEventLoopGroup(
          0,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        true);
      try {
        conn.open();
        assertThat(conn.getLdapURL()).isNotNull();
        assertThat(conn.getLdapURL().getScheme()).isEqualTo("ldap");
        assertThat(conn.getLdapURL().getHostname()).isEqualTo(address.getHostName());
        assertThat(conn.getLdapURL().getPort()).isEqualTo(address.getPort());
        assertThat(conn.getLdapURL().getUrl().isDefaultBaseDn()).isFalse();
        assertThat(conn.getLdapURL().getUrl().getBaseDn()).isEqualTo("dc=ldaptive,dc=org");
        assertThat(conn.getLdapURL().getUrl().isDefaultAttributes()).isFalse();
        assertThat(conn.getLdapURL().getUrl().getAttributes()).isEqualTo(new String[] {"cn", "sn"});
        assertThat(conn.getLdapURL().getUrl().isDefaultScope()).isFalse();
        assertThat(conn.getLdapURL().getUrl().getScope()).isEqualTo(SearchScope.ONELEVEL);
        assertThat(conn.getLdapURL().getUrl().isDefaultFilter()).isFalse();
        assertThat(conn.getLdapURL().getUrl().getFilter()).isEqualTo("(uid=dfisher)");
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }


  @Test(groups = "netty")
  public void ldapsWithStartTLS()
    throws Exception
  {
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final NettyConnection conn = new NettyConnection(
        ConnectionConfig.builder()
          .url("ldaps://" + address.getHostName() + ":" + address.getPort())
          .useStartTLS(true)
          .build(),
        NioSocketChannel.class,
        new NioEventLoopGroup(
          0,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        true);
      try {
        conn.open();
        fail("Should have thrown exception");
      } catch (IllegalStateException e) {
        assertThat(e).isExactlyInstanceOf(IllegalStateException.class);
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }


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
      final NettyConnection conn = new NettyConnection(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          .build(),
        NioSocketChannel.class,
        new NioEventLoopGroup(
          0,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        true);
      try {
        conn.open();
        assertThat(conn.isOpen()).isTrue();
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @param  eventLoopGroup  to supply to the connection
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "threadPools", groups = "netty")
  public void openAndReconnect(final NioEventLoopGroup eventLoopGroup)
    throws Exception
  {
    final CountDownLatch openLatch = new CountDownLatch(2);
    final SimpleNettyServer server = new SimpleNettyServer(
      ctx -> openLatch.countDown(),
      (ctx, msg) -> {
        if (msg instanceof UnbindRequest) {
          ctx.fireUserEventTriggered(SimpleNettyServer.Event.DISCONNECT);
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final AtomicBoolean reconnectAttempted = new AtomicBoolean();
      final ConnectionConfig connConfig = ConnectionConfig.builder()
        .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
        .autoReconnect(true)
        .autoReconnectCondition(metadata -> {
          assertThat(metadata.getAttempts()).isEqualTo(0);
          reconnectAttempted.set(true);
          return metadata instanceof ClosedRetryMetadata && metadata.getAttempts() == 0;
        })
        .build();
      final NettyConnection conn = new NettyConnection(
        connConfig,
        NioSocketChannel.class,
        eventLoopGroup,
        null,
        false);
      try {
        conn.open();
        assertThat(conn.isOpen()).isTrue();
        // unbind will cause the server to disconnect
        conn.operation(new UnbindRequest());
        if (!openLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          fail("Connection did not reconnect");
        }
        assertThat(reconnectAttempted.get()).isTrue();
        // it may take a few seconds for the connection to reestablish
        int isOpenCount = 0;
        while (!conn.isOpen() && isOpenCount < 10) {
          Thread.sleep(1000);
          isOpenCount++;
        }
        assertThat(conn.isOpen()).isTrue();
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @param  eventLoopGroup  to supply to the connection
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "threadPools", groups = "netty")
  public void connectionValidator(final NioEventLoopGroup eventLoopGroup)
    throws Exception
  {
    final CountDownLatch validateLatch = new CountDownLatch(1);
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final NettyConnection conn = new NettyConnection(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          // CheckStyle:AnonInnerLength OFF
          .connectionValidator(new ConnectionValidator() {
            @Override
            public void applyAsync(final Connection conn, final Consumer<Boolean> function)
            {
              validateLatch.countDown();
              function.accept(true);
            }

            @Override
            public Supplier<Boolean> applyAsync(final Connection conn)
            {
              throw new UnsupportedOperationException();
            }

            @Override
            public Duration getValidatePeriod()
            {
              return Duration.ofSeconds(5);
            }

            @Override
            public Duration getValidateTimeout()
            {
              return Duration.ofSeconds(1);
            }

            @Override
            public Boolean apply(final Connection conn)
            {
              throw new UnsupportedOperationException();
            }
          })
          // CheckStyle:AnonInnerLength ON
          .build(),
        NioSocketChannel.class,
        eventLoopGroup,
        null,
        false);
      try {
        conn.open();
        assertThat(conn.isOpen()).isTrue();
        if (!validateLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          fail("Connection validator did not execute");
        }
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }


  /**
   * @param  eventLoopGroup  to supply to the connection
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "threadPools", groups = "netty")
  public void connectionValidatorReconnect(final NioEventLoopGroup eventLoopGroup)
    throws Exception
  {
    final CountDownLatch validateLatch = new CountDownLatch(2);
    final SimpleNettyServer server = new SimpleNettyServer();
    try {
      final InetSocketAddress address = server.start();
      final NettyConnection conn = new NettyConnection(
        ConnectionConfig.builder()
          .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
          // CheckStyle:AnonInnerLength OFF
          .connectionValidator(new ConnectionValidator() {
            @Override
            public void applyAsync(final Connection conn, final Consumer<Boolean> function)
            {
              if (validateLatch.getCount() == 2) {
                function.accept(false);
              } else {
                function.accept(true);
              }
              validateLatch.countDown();
            }

            @Override
            public Supplier<Boolean> applyAsync(final Connection conn)
            {
              throw new UnsupportedOperationException();
            }

            @Override
            public Duration getValidatePeriod()
            {
              return Duration.ofSeconds(5);
            }

            @Override
            public Duration getValidateTimeout()
            {
              return Duration.ofSeconds(1);
            }

            @Override
            public Boolean apply(final Connection conn)
            {
              throw new UnsupportedOperationException();
            }
          })
          // CheckStyle:AnonInnerLength ON
          .build(),
        NioSocketChannel.class,
        eventLoopGroup,
        null,
        false);
      try {
        conn.open();
        assertThat(conn.isOpen()).isTrue();
        if (!validateLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          fail("Connection validator did not execute");
        }
        assertThat(conn.isOpen()).isTrue();
      } finally {
        conn.close();
        assertThat(conn.isOpen()).isFalse();
      }
    } finally {
      server.stop();
    }
  }
}
