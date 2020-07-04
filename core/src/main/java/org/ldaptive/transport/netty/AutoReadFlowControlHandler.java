/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.util.concurrent.atomic.AtomicInteger;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps a counter of messages that have been sent down the pipeline. That counter is decremented whenever a read is
 * requested. A read is only propagated to the channel when all messages have completed. This handler is intended to be
 * used with {@link NettyConnection.AutoReadEventHandler}.
 *
 * @author  Middleware Services
 */
public class AutoReadFlowControlHandler extends ChannelDuplexHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Number of messages in the pipeline. */
  private final AtomicInteger messageCount = new AtomicInteger();


  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg)
    throws Exception
  {
    if (!ctx.channel().config().isAutoRead()) {
      logger.trace("channelRead with message count of {}", messageCount);
      messageCount.incrementAndGet();
    }
    ctx.fireChannelRead(msg);
  }


  @Override
  public void read(final ChannelHandlerContext ctx)
    throws Exception
  {
    if (!ctx.channel().config().isAutoRead()) {
      logger.trace("read with message count of {}", messageCount);
      if (messageCount.updateAndGet(i -> i > 0 ? i - 1 : 0) == 0) {
        ctx.read();
      }
    } else {
      ctx.read();
    }
  }
}
