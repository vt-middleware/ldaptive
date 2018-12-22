/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import org.ldaptive.ResultCode;
import org.ldaptive.control.PagedResultsControl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchResultDone}.
 *
 * @author  Middleware Services
 */
public class SearchResultDoneTest
{


  /**
   * Search result done response test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          // success search result done response
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          new SearchResultDone.Builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("").build(),
        },
        new Object[] {
          // referral search result done response with referrals
          new byte[] {
            // preamble
            0x30, 0x6f, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x6a,
            // referral result
            0x0a, 0x01, 0x0a,
            // matched DN
            0x04, 0x19, 0x6f, 0x75, 0x3d, 0x72, 0x65, 0x66, 0x65, 0x72, 0x72, 0x61, 0x6c, 0x73, 0x2c, 0x64, 0x63, 0x3d,
            0x76, 0x74, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x64, 0x75,
            // no diagnostic message
            0x04, 0x00,
            // referral URL
            (byte) 0xa3, 0x48, 0x04, 0x46, 0x6c, 0x64, 0x61, 0x70, 0x3a, 0x2f, 0x2f, 0x6c, 0x64, 0x61, 0x70, 0x2d, 0x74,
            0x65, 0x73, 0x74, 0x2d, 0x31, 0x2e, 0x6d, 0x69, 0x64, 0x64, 0x6c, 0x65, 0x77, 0x61, 0x72, 0x65, 0x2e, 0x76,
            0x74, 0x2e, 0x65, 0x64, 0x75, 0x3a, 0x31, 0x30, 0x33, 0x38, 0x39, 0x2f, 0x6f, 0x75, 0x3d, 0x70, 0x65, 0x6f,
            0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x76, 0x74, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x64, 0x75, 0x3f, 0x3f,
            0x6f, 0x6e, 0x65},
          new SearchResultDone.Builder().messageID(2)
            .resultCode(ResultCode.REFERRAL)
            .matchedDN("ou=referrals,dc=vt,dc=edu")
            .diagnosticMessage("")
            .referralURLs("ldap://ldap-test-1.middleware.vt.edu:10389/ou=people,dc=vt,dc=edu??one").build(),
        },
        new Object[] {
          // success search result done response with paged results
          new byte[] {
            // preamble
            0x30, 0x39, 0x02, 0x01, 0x02,
            // search result done
            0x65, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // response control
            (byte) 0xa0, 0x2b, 0x30, 0x29,
            // control oid
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x33, 0x31, 0x39,
            // paged results value
            0x04, 0x0f, 0x30, 0x0d, 0x02, 0x01, 0x00, 0x04, 0x08, 0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00},
          new SearchResultDone.Builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .controls(
              new PagedResultsControl(0, new byte[] {0x5a, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, false))
            .build(),
        },
      };
  }

  /**
   * @param  berValue  encoded response.
   * @param  response  expected decoded response.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "response")
  public void encode(final byte[] berValue, final SearchResultDone response)
    throws Exception
  {
    Assert.assertEquals(new SearchResultDone(ByteBuffer.wrap(berValue)), response);
  }
}
