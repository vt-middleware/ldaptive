/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.ldaptive.ClosedRetryMetadata;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.UnbindRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link NettyConnection}.
 *
 * @author  Middleware Services
 */
public class NettyConnectionTest
{


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
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void openAndReconnectDefaultThreads()
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
        new NioEventLoopGroup(
          0,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        null,
        true);
      try {
        conn.open();
        Assert.assertTrue(conn.isOpen());
        // unbind will cause the server to disconnect
        conn.operation(new UnbindRequest());
        if (!openLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection did not reconnect");
        }
        Assert.assertTrue(reconnectAttempted.get());
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
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void openAndReconnectOneThread()
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
        new NioEventLoopGroup(
          1,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        null,
        true);
      try {
        conn.open();
        Assert.assertTrue(conn.isOpen());
        // unbind will cause the server to disconnect
        conn.operation(new UnbindRequest());
        if (!openLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection did not reconnect");
        }
        Assert.assertTrue(reconnectAttempted.get());
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
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void openAndReconnectTwoThreads()
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
        new NioEventLoopGroup(
          2,
          new ThreadPerTaskExecutor(new DefaultThreadFactory(NettyConnectionTest.class, true, Thread.NORM_PRIORITY))),
        null,
        null,
        true);
      try {
        conn.open();
        Assert.assertTrue(conn.isOpen());
        // unbind will cause the server to disconnect
        conn.operation(new UnbindRequest());
        if (!openLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection did not reconnect");
        }
        Assert.assertTrue(reconnectAttempted.get());
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
