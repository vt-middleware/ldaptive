/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.buffer.Unpooled;
import org.ldaptive.Message;
import org.ldaptive.transport.ResponseParser;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ResponseParser} that uses a {@link NettyDERBuffer}.
 *
 * @author  Middleware Services
 */
public class ResponseParserTest extends org.ldaptive.transport.ResponseParserTest
{


  /**
   * @param  berValue  to parse.
   * @param  response  expected response.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport", dataProvider = "response")
  public void parse(final byte[] berValue, final Message response)
    throws Exception
  {
    final ResponseParser parser = new ResponseParser();
    assertThat(parser.parse(new NettyDERBuffer(Unpooled.wrappedBuffer(berValue))).get()).isEqualTo(response);
  }
}
