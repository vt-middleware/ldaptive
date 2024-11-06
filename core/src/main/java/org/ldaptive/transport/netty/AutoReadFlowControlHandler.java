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
final class AutoReadFlowControlHandler extends ChannelDuplexHandler
{

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Number of messages in the pipeline. */
  private final AtomicInteger messageCount = new AtomicInteger();


  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg)
    throws Exception
  {
    // increments the message count which blocks further reads until all inbound messages have been processed
    logger.trace("channel read with message count of {} on {}", messageCount, ctx);
    messageCount.incrementAndGet();
    logger.trace("invoking fireChannelRead with message count {} for {} on {}", messageCount, msg, ctx);
    ctx.fireChannelRead(msg);
  }


  @Override
  public void read(final ChannelHandlerContext ctx)
    throws Exception
  {
    // prevents outbound handlers from reading more data until all inbound messages have been read
    logger.trace("read with message count of {} on {}", messageCount, ctx);
    if (messageCount.updateAndGet(i -> i > 0 ? i - 1 : 0) == 0) {
      logger.trace("invoking read with message count {} on {}", messageCount, ctx);
      ctx.read();
    }
  }
}
