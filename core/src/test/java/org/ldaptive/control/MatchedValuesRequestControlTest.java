/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link MatchedValuesRequestControl}.
 *
 * @author  Middleware Services
 */
public class MatchedValuesRequestControlTest
{


  /**
   * Matched values request control test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // presence filter
        // BER: 30:16:87:14:65:64:75:50:65:72:73:6F:6E:41:66:66:69:6C:69:61:74:69:6F:6E
        new Object[] {
          LdapUtils.base64Decode("MBaHFGVkdVBlcnNvbkFmZmlsaWF0aW9u"),
          new MatchedValuesRequestControl("(eduPersonAffiliation=*)"),
        },
        // equality filter
        // BER: 30:21:A3:1F:04:14:65:64:75:50:65:72:73:6F:6E:41:66:66:69:6C:69:61:74:69:6F:6E:04:07:53:54:55:44:45:4E:54
        new Object[] {
          LdapUtils.base64Decode("MCGjHwQUZWR1UGVyc29uQWZmaWxpYXRpb24EB1NUVURFTlQ="),
          new MatchedValuesRequestControl("(eduPersonAffiliation=STUDENT)"),
        },
        // substring filter
        // BER: 30:20:A4:1E:04:14:65:64:75:50:65:72:73:6F:6E:41:66:66:69:6C:69:61:74:69:6F:6E:30:06:81:04:61:6C:75:6D
        new Object[] {
          LdapUtils.base64Decode("MCCkHgQUZWR1UGVyc29uQWZmaWxpYXRpb24wBoEEYWx1bQ=="),
          new MatchedValuesRequestControl("(eduPersonAffiliation=*alum*)"),
        },
        // extensible filter
        // BER: 30:31:A9:2F:81:0E:63:61:73:65:45:78:61:63:74:4D:61:74:63:68:82:14:65:64:75:50:65:72:73:6F:6E:41:66:66:69
        //      6C:69:61:74:69:6F:6E:83:0A:53:54:55:44:45:4E:54
        new Object[] {
          LdapUtils.base64Decode("MDGpL4EOY2FzZUV4YWN0TWF0Y2iCFGVkdVBlcnNvbkFmZmlsaWF0aW9ugwdTVFVERU5U"),
          new MatchedValuesRequestControl("(eduPersonAffiliation:caseExactMatch:=STUDENT)"),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  control  matched values request control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request")
  public void encode(final byte[] berValue, final MatchedValuesRequestControl control)
    throws Exception
  {
    assertThat(control.encode()).isEqualTo(berValue);
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control")
  public void invalidFilter()
    throws Exception
  {
    try {
      new MatchedValuesRequestControl("(&(uid=1)(gn=bob))");
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    try {
      new MatchedValuesRequestControl("(|(uid=1)(gn=bob))");
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    try {
      new MatchedValuesRequestControl("(!(gn=bob))");
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    try {
      new MatchedValuesRequestControl("(:dn:2.5.13.5:=John)");
      fail("Should have thrown IllegalArgumentException");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }
}
