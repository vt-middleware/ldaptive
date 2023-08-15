/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import org.ldaptive.AddResponse;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareResponse;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Message;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchResponse;
import org.ldaptive.extended.ExtendedResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for inbound channel handlers.
 *
 * @author  Middleware Services
 */
public class ChannelInboundHandlerTest
{


  @Test
  public void startTLSResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x0c, 0x02, 0x01, 0x01, 0x78, 0x07, 0x0a, 0x01, 0x00, 0x04, 0x00, 0x04, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      ExtendedResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("")
        .diagnosticMessage("")
        .build());

    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, (byte) 0x84, 0x00, 0x00, 0x00, 0x28, 0x02, 0x01, 0x01, 0x78, (byte) 0x84, 0x00, 0x00, 0x00, 0x1f, 0x0a,
      0x01, 0x00, 0x04, 0x00, 0x04, 0x00, (byte) 0x8a, 0x16, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e,
      0x34, 0x2e, 0x31, 0x2e, 0x31, 0x34, 0x36, 0x36, 0x2e, 0x32, 0x30, 0x30, 0x33, 0x37,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      ExtendedResponse.builder()
        .messageID(1)
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("")
        .diagnosticMessage("")
        .responseName("1.3.6.1.4.1.1466.20037")
        .build());
  }


  @Test
  public void bindResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x0c, 0x02, 0x01, 0x02, 0x61, 0x07, 0x0a, 0x01, 0x00, 0x04, 0x00, 0x04, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      BindResponse.builder()
        .messageID(2)
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("")
        .diagnosticMessage("")
        .build());
  }


  @Test
  public void addResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x0c, 0x02, 0x01, 0x03, 0x69, 0x07, 0x0a, 0x01, 0x00, 0x04, 0x00, 0x04, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      AddResponse.builder()
        .messageID(3)
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("")
        .diagnosticMessage("")
        .build());
  }


  @Test
  public void compareResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x0d, 0x02, 0x02, 0x02, 0x06, 0x6f, 0x07, 0x0a, 0x01, 0x06, 0x04, 0x00, 0x04, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      CompareResponse.builder()
        .messageID(518)
        .resultCode(ResultCode.COMPARE_TRUE)
        .matchedDN("")
        .diagnosticMessage("")
        .build());

    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, (byte) 0x84, 0x00, 0x00, 0x00, 0x10, 0x02, 0x01, 0x23, 0x6f, (byte) 0x84, 0x00, 0x00, 0x00, 0x07, 0x0a,
      0x01, 0x06, 0x04, 0x00, 0x04, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      CompareResponse.builder()
        .messageID(35)
        .resultCode(ResultCode.COMPARE_TRUE)
        .matchedDN("")
        .diagnosticMessage("")
        .build());
  }


  @Test
  public void ldapEntryResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x09, 0x02, 0x01, 0x5f, 0x64, 0x04, 0x04, 0x00, 0x30, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      LdapEntry.builder()
        .messageID(95)
        .dn("")
        .build());
  }


  @Test
  public void ldapEntryMultiFrameResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);

    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x49, 0x02, 0x01, 0x02, 0x64, 0x44, 0x04, 0x11, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65,
    }));
    Assert.assertNull(messageHandler.getMessage());

    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x2c, 0x64, 0x63, 0x3d, 0x63, 0x6f, 0x6d, 0x30, 0x2f, 0x30, 0x1c, 0x04, 0x0b, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74,
    }));
    Assert.assertNull(messageHandler.getMessage());

    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x43, 0x6c, 0x61, 0x73, 0x73, 0x31, 0x0d, 0x04, 0x03, 0x74, 0x6f, 0x70, 0x04, 0x06, 0x64, 0x6f, 0x6d, 0x61, 0x69,
    }));
    Assert.assertNull(messageHandler.getMessage());

    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x6e, 0x30, 0x0f, 0x04, 0x02, 0x64, 0x63, 0x31, 0x09, 0x04, 0x07, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      LdapEntry.builder()
        .messageID(2)
        .dn("dc=example,dc=com")
        .attributes(new LdapAttribute("objectClass", "top", "domain"), new LdapAttribute("dc", "example")).build());
  }


  @Test
  public void searchResponse()
  {
    final TestMessageHandler messageHandler = new TestMessageHandler();
    final EmbeddedChannel channel = new EmbeddedChannel(
      new MessageFrameDecoder(), new NettyConnection.MessageDecoder(), messageHandler);
    channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {
      0x30, 0x0c, 0x02, 0x01, 0x5f, 0x65, 0x07, 0x0a, 0x01, 0x00, 0x04, 0x00, 0x04, 0x00,
    }));
    Assert.assertEquals(
      messageHandler.getMessage(),
      SearchResponse.builder()
        .messageID(95)
        .resultCode(ResultCode.SUCCESS)
        .matchedDN("")
        .diagnosticMessage("")
        .build());
  }


  /** Test message handler. */
  private static final class TestMessageHandler extends SimpleChannelInboundHandler<Message>
  {
    /** Message read by this handler. */
    private Message message;


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final Message msg)
    {
      message = msg;
    }


    public Message getMessage()
    {
      return message;
    }
  }
}
