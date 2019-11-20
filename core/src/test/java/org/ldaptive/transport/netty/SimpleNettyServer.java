/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.ldaptive.Request;
import org.ldaptive.transport.RequestParser;

/**
 * Simple server for testing TCP connections.
 *
 * @author  Middleware Services
 */
public class SimpleNettyServer
{

  /** Custom events to facilitate testing. */
  public enum Event {
    /** Force a client disconnect. */
    DISCONNECT,
  }

  /** Open notifications. */
  private Consumer<ChannelHandlerContext> onOpen;

  /** Message notifications. */
  private BiConsumer<ChannelHandlerContext, Request> onMessage;

  /** Close notifications. */
  private Consumer<ChannelHandlerContext> onClose;

  /** Channel future. */
  private ChannelFuture channelFuture;


  /** Default constructor. */
  public SimpleNettyServer() {}


  /**
   * Creates a new simple netty server.
   *
   * @param  messageCondition  to execute on message
   */
  public SimpleNettyServer(final BiConsumer<ChannelHandlerContext, Request> messageCondition)
  {
    onMessage = messageCondition;
  }


  /**
   * Creates a new simple netty server.
   *
   * @param  openCondition  to execute on open
   * @param  messageCondition  to execute on message
   * @param  closeCondition  to execute on close
   */
  public SimpleNettyServer(
    final Consumer<ChannelHandlerContext> openCondition,
    final BiConsumer<ChannelHandlerContext, Request> messageCondition,
    final Consumer<ChannelHandlerContext> closeCondition)
  {
    onOpen = openCondition;
    onMessage = messageCondition;
    onClose = closeCondition;
  }


  /**
   * Start the server.
   *
   * @return  socket address the server is listening on
   *
   * @throws  InterruptedException  if the server is interrupted
   */
  public InetSocketAddress start()
    throws InterruptedException
  {
    final EventLoopGroup bossGroup = new NioEventLoopGroup();
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    final ServerBootstrap bootstrap = new ServerBootstrap();
    // CheckStyle:AnonInnerLength OFF
    bootstrap.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .localAddress("localhost", 0)
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(final SocketChannel ch)
        {
          ch.pipeline().addLast("frame_decoder", new MessageFrameDecoder());
          ch.pipeline().addLast("request_decoder", new ByteToMessageDecoder() {
            @Override
            protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out)
            {
              final RequestParser parser = new RequestParser();
              final Optional<Request> message =  parser.parse(new NettyDERBuffer(in));
              message.ifPresent(out::add);
            }
          });
          ch.pipeline().addLast("message_handler", new SimpleChannelInboundHandler<Request>() {
            @Override
            protected void channelRead0(final ChannelHandlerContext ctx, final Request msg)
            {
              if (onMessage != null) {
                onMessage.accept(ctx, msg);
              }
            }
          });
          ch.pipeline().addLast("event_handler", new SimpleUserEventChannelHandler<Event>() {
            @Override
            protected void eventReceived(final ChannelHandlerContext ctx, final Event evt)
            {
              if (Event.DISCONNECT == evt) {
                ctx.close();
              }
            }
          });
          ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(final ChannelHandlerContext ctx)
              throws Exception
            {
              if (onOpen != null) {
                onOpen.accept(ctx);
              }
              super.channelActive(ctx);
            }
            @Override
            public void channelInactive(final ChannelHandlerContext ctx)
              throws Exception
            {
              if (onClose != null) {
                onClose.accept(ctx);
              }
              super.channelInactive(ctx);
            }
            @Override
            public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause)
            {
              cause.printStackTrace();
            }
          });
        }
      })
      .childOption(ChannelOption.SO_KEEPALIVE, true);
      // CheckStyle:AnonInnerLength ON
    channelFuture = bootstrap.bind().sync();
    if (!channelFuture.isSuccess()) {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      throw new IllegalStateException("Could not start server", channelFuture.cause());
    }
    channelFuture.channel().closeFuture().addListener(f -> {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    });
    return (InetSocketAddress) channelFuture.channel().localAddress();
  }


  /**
   * Stop the server.
   *
   * @throws  InterruptedException  if the channel close is interrupted
   */
  public void stop()
    throws InterruptedException
  {
    if (channelFuture != null) {
      channelFuture.channel().close().sync();
    }
  }
}
