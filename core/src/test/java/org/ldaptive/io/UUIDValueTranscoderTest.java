/*
  $Id: UUIDValueTranscoderTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.io;

import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link UUIDValueTranscoder}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
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
  @Test(
    groups = {"io"},
    dataProvider = "uuids"
  )
  public void testTranscode(final String s)
    throws Exception
  {
    final UUID uuid = UUID.fromString(s);
    Assert.assertEquals(uuid, transcoder.decodeStringValue(s));
    Assert.assertEquals(s, transcoder.encodeStringValue(uuid));
  }
}
