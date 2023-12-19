/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.SearchRequest;
import org.ldaptive.transport.netty.SimpleNettyServer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SyncReplRunner}.
 *
 * @author  Middleware Services
 */
public class SyncReplRunnerTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "netty")
  public void validateConnection()
    throws Exception
  {
    final CountDownLatch openLatch = new CountDownLatch(2);
    final SimpleNettyServer server = new SimpleNettyServer(
      ctx -> openLatch.countDown(),
      (ctx, msg) -> {
        if (msg instanceof SearchRequest) {
          ctx.fireUserEventTriggered(SimpleNettyServer.Event.DISCONNECT);
        }
      },
      null);
    try {
      final InetSocketAddress address = server.start();
      final SyncReplRunner runner = new SyncReplRunner(
        SyncReplRunner.createConnectionFactory(
          ConnectionConfig.builder()
            .url(new LdapURL(address.getHostName(), address.getPort()).getHostnameWithSchemeAndPort())
            .build(),
          SearchConnectionValidator.builder().period(Duration.ofSeconds(5)).build()),
        SearchRequest.builder().filter("(objectClass=*)").build(),
        new DefaultCookieManager());
      try {
        runner.initialize();
        runner.start();
        if (!openLatch.await(Duration.ofMinutes(1).toMillis(), TimeUnit.MILLISECONDS)) {
          Assert.fail("Connection did not reconnect");
        }
      } finally {
        runner.stop();
      }
    } finally {
      server.stop();
    }
  }
}
