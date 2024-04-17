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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
        Assert.assertTrue(conn.isOpen());
        int id = conn.getMessageID();
        Assert.assertEquals(id, 1);
        conn.operation(new UnbindRequest());
        Assert.assertEquals(conn.getMessageID(), id + 1);
        conn.setMessageID(Integer.MAX_VALUE - 1);
        conn.operation(new UnbindRequest());
        id = conn.getMessageID();
        Assert.assertEquals(id, Integer.MAX_VALUE);
        conn.operation(new UnbindRequest());
        id = conn.getMessageID();
        Assert.assertEquals(id, 1);
        conn.operation(new UnbindRequest());
        Assert.assertEquals(conn.getMessageID(), id + 1);
      } finally {
        conn.close();
        Assert.assertFalse(conn.isOpen());
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
    Assert.assertNotNull(options);
    Assert.assertEquals(options.get(ChannelOption.AUTO_READ), false);
    Assert.assertEquals(options.get(ChannelOption.TCP_NODELAY), true);
    Assert.assertEquals(options.get(ChannelOption.SO_SNDBUF), 1024);
    Assert.assertEquals(options.get(ChannelOption.SO_RCVBUF), 1024);
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
        Assert.assertNotNull(conn.getLdapURL());
        Assert.assertEquals(conn.getLdapURL().getScheme(), "ldap");
        Assert.assertEquals(conn.getLdapURL().getHostname(), address.getHostName());
        Assert.assertEquals(conn.getLdapURL().getPort(), address.getPort());
        Assert.assertFalse(conn.getLdapURL().getUrl().isDefaultBaseDn());
        Assert.assertEquals(conn.getLdapURL().getUrl().getBaseDn(), "dc=ldaptive,dc=org");
        Assert.assertFalse(conn.getLdapURL().getUrl().isDefaultAttributes());
        Assert.assertEquals(conn.getLdapURL().getUrl().getAttributes(), new String[] {"cn", "sn"});
        Assert.assertFalse(conn.getLdapURL().getUrl().isDefaultScope());
        Assert.assertEquals(conn.getLdapURL().getUrl().getScope(), SearchScope.ONELEVEL);
        Assert.assertFalse(conn.getLdapURL().getUrl().isDefaultFilter());
        Assert.assertEquals(conn.getLdapURL().getUrl().getFilter(), "(uid=dfisher)");
      } finally {
        conn.close();
        Assert.assertFalse(conn.isOpen());
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
        Assert.assertTrue(conn.isOpen());
      } finally {
        conn.close();
        Assert.assertFalse(conn.isOpen());
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
          Assert.assertEquals(metadata.getAttempts(), 0);
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
        Assert.assertTrue(conn.isOpen());
        // unbind will cause the server to disconnect
        conn.operation(new UnbindRequest());
        if (!openLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection did not reconnect");
        }
        Assert.assertTrue(reconnectAttempted.get());
        // it may take a few seconds for the connection to reestablish
        int isOpenCount = 0;
        while (!conn.isOpen() && isOpenCount < 10) {
          Thread.sleep(1000);
          isOpenCount++;
        }
        Assert.assertTrue(conn.isOpen());
      } finally {
        conn.close();
        Assert.assertFalse(conn.isOpen());
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
        Assert.assertTrue(conn.isOpen());
        if (!validateLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection validator did not execute");
        }
      } finally {
        conn.close();
        Assert.assertFalse(conn.isOpen());
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
        Assert.assertTrue(conn.isOpen());
        if (!validateLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection validator did not execute");
        }
        Assert.assertTrue(conn.isOpen());
      } finally {
        conn.close();
        Assert.assertFalse(conn.isOpen());
      }
    } finally {
      server.stop();
    }
  }
}
