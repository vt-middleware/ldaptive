/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.transport.ThreadPoolConfig;
import org.ldaptive.transport.Transport;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link NettyTransportFactory}.
 *
 * @author  Middleware Services
 */
public class NettyTransportFactoryTest
{

  /** Factory to test. */
  private final NettyTransportFactory factory = new NettyTransportFactory();


  /**
   * Returns transport configurations to test.
   *
   * @return  thread pools
   */
  @DataProvider(name = "transport-configs")
  public Object[][] createTransportConfigs()
  {
    return
      new Object[][] {
        new Object[] {
          ThreadPoolConfig.singleIoThread("test-factory", ThreadPoolConfig.ShutdownStrategy.CONNECTION_CLOSE),
        },
        new Object[] {
          ThreadPoolConfig.singleIoThread("test-factory", ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE),
        },
        new Object[] {
          ThreadPoolConfig.singleIoThread("test-factory", ThreadPoolConfig.ShutdownStrategy.NEVER),
        },
        new Object[] {
          ThreadPoolConfig.defaultIoThreads("test-factory", ThreadPoolConfig.ShutdownStrategy.CONNECTION_CLOSE),
        },
        new Object[] {
          ThreadPoolConfig.defaultIoThreads("test-factory", ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE),
        },
        new Object[] {
          ThreadPoolConfig.defaultIoThreads("test-factory", ThreadPoolConfig.ShutdownStrategy.NEVER),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(0)
            .messageThreads(0)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_CLOSE)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(1)
            .messageThreads(5)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_CLOSE)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(1)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.NEVER)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(1)
            .messageThreads(5)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.NEVER)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(0)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(0)
            .messageThreads(0)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(1)
            .messageThreads(0)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .freeze()
            .build(),
        },
        new Object[] {
          ThreadPoolConfig.builder()
            .threadPoolName("test-factory")
            .ioThreads(0)
            .messageThreads(1)
            .shutdownStrategy(ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE)
            .freeze()
            .build(),
        },
      };
  }


  /**
   * @param  config  transport config to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "transport-configs", groups = "netty")
  public void createTransport(final ThreadPoolConfig config)
    throws Exception
  {
    final Transport t = factory.createTransport(config);
    if (config.getShutdownStrategy() == ThreadPoolConfig.ShutdownStrategy.CONNECTION_CLOSE) {
      assertThat(t).isExactlyInstanceOf(DefaultNettyTransport.class);
    } else if (config.getShutdownStrategy() == ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE) {
      assertThat(t).isExactlyInstanceOf(StatefulNettyTransport.class);
      assertThat(((StatefulNettyTransport) t).getShutdownOnClose()).isTrue();
    } else if (config.getShutdownStrategy() == ThreadPoolConfig.ShutdownStrategy.NEVER) {
      assertThat(t).isExactlyInstanceOf(StatefulNettyTransport.class);
      assertThat(((StatefulNettyTransport) t).getShutdownOnClose()).isFalse();
    } else {
      fail("Unexpected shutdown strategy: {}", config.getShutdownStrategy());
    }

    final NettyConnection conn = (NettyConnection) t.create(
      ConnectionConfig.builder().url("ldap://directory.ldaptive.org").build());
    assertThat(conn.getIoWorkerGroup()).isNotNull();
    final MultithreadEventExecutorGroup ioGroup = (MultithreadEventExecutorGroup) conn.getIoWorkerGroup();
    if (config.getIoThreads() == 0) {
      assertThat(ioGroup.executorCount()).isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
    } else {
      assertThat(ioGroup.executorCount()).isEqualTo(config.getIoThreads());
    }
    if (config.getMessageThreads() == -1) {
      assertThat(conn.getMessageWorkerGroup()).isNull();
    } else {
      assertThat(conn.getMessageWorkerGroup()).isNotNull();
      final MultithreadEventExecutorGroup msgGroup = (MultithreadEventExecutorGroup) conn.getMessageWorkerGroup();
      if (config.getMessageThreads() == 0) {
        assertThat(msgGroup.executorCount()).isEqualTo(Runtime.getRuntime().availableProcessors() * 2);
      } else {
        assertThat(msgGroup.executorCount()).isEqualTo(config.getMessageThreads());
      }
    }
  }
}
