/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link UUIDValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class UUIDValueTranscoderTest
{

  /** Transcoder to test. */
  private final UUIDValueTranscoder transcoder = new UUIDValueTranscoder();


  /**
   * UUID test data.
   *
   * @return  ldap attribute values
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "uuids")
  public Object[][] createUuids()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {"313def52-1e6b-102a-99ba-d6537704ad77", },
        new Object[] {"86641130-5f20-1031-8c17-394bb0fda920", },
        new Object[] {"7bb84c1c-1b19-102a-8e02-cbbda55d336d", },
        new Object[] {"76988e4a-1b19-102a-81f0-cbbda55d336d", },
        new Object[] {"05411a1a-1b18-102a-97aa-cbbda55d336d", },
        new Object[] {"ff8eeaca-1b17-102a-88c1-cbbda55d336d", },
        new Object[] {"3973914a-1b19-102a-8aab-cbbda55d336d", },
      };
  }


  /**
   * @param  s  uuid to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "uuids")
  public void testTranscode(final String s)
    throws Exception
  {
    final UUID uuid = UUID.fromString(s);
    Assert.assertEquals(uuid, transcoder.decodeStringValue(s));
    Assert.assertEquals(s, transcoder.encodeStringValue(uuid));
  }
}
