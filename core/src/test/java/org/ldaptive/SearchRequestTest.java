/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.ldaptive.control.ProxyAuthorizationControl;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.filter.AndFilter;
import org.ldaptive.filter.EqualityFilter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SearchRequest}.
 *
 * @author  Middleware Services
 */
public class SearchRequestTest
{


  /**
   * Search test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          SearchRequest.builder()
            .dn("dc=example,dc=com")
            .scope(SearchScope.SUBTREE)
            .aliases(DerefAliases.NEVER)
            .sizeLimit(1000)
            .timeLimit(Duration.ofSeconds(30))
            .typesOnly(false)
            .filter(new AndFilter(new EqualityFilter("objectClass", "person"), new EqualityFilter("uid", "jdoe")))
            .returnAttributes(new String[] {"*", "+"}).build(),
          new byte[] {
            // preamble
            0x30, 0x56, 0x02, 0x01, 0x02,
            // search op
            0x63, 0x51,
            // base dn
            0x04, 0x11, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63, 0x6f,
            0x6d,
            // subtree scope
            0x0a, 0x01, 0x02,
            // never deref aliases
            0x0a, 0x01, 0x00,
            // size limit 1000
            0x02, 0x02, 0x03, (byte) 0xe8,
            // time limit 30
            0x02, 0x01, 0x1e,
            // types only false
            0x01, 0x01, 0x00,
            // start AND filter
            (byte) 0xa0, 0x24,
            // start equality filter
            (byte) 0xa3, 0x15,
            // attribute of objectClass
            0x04, 0x0b, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x43, 0x6c, 0x61, 0x73, 0x73,
            // assertion of person
            0x04, 0x06, 0x70, 0x65, 0x72, 0x73, 0x6f, 0x6e,
            // start equality filter
            (byte) 0xa3, 0x0b,
            // attribute of uid
            0x04, 0x03, 0x75, 0x69, 0x64,
            // assertion of jdoe
            0x04, 0x04, 0x6a, 0x64, 0x6f, 0x65,
            // request all user and operational attributes
            0x30, 0x06, 0x04, 0x01, 0x2a, 0x04, 0x01, 0x2b, },
        },
        new Object[] {
          SearchRequest.builder()
            .dn("ou=test,dc=vt,dc=edu")
            .scope(SearchScope.SUBTREE)
            .aliases(DerefAliases.NEVER)
            .sizeLimit(0)
            .timeLimit(Duration.ofSeconds(0))
            .typesOnly(false)
            .filter("(CN=John Adams)")
            .returnAttributes(new String[] {"*"})
            .controls(new ProxyAuthorizationControl("dn:")).build(),
          new byte[] {
            // preamble
            0x30, 0x67, 0x02, 0x01, 0x02,
            // search op
            0x63, 0x3c,
            // base dn
            0x04, 0x14, 0x6f, 0x75, 0x3d, 0x74, 0x65, 0x73, 0x74, 0x2c, 0x64, 0x63, 0x3d, 0x76, 0x74, 0x2c, 0x64, 0x63,
            0x3d, 0x65, 0x64, 0x75,
            // subtree scope
            0x0a, 0x01, 0x02,
            // never deref aliases
            0x0a, 0x01, 0x00,
            // size limit 0
            0x02, 0x01, 0x00,
            // time limit 0
            0x02, 0x01, 0x00,
            // types only false
            0x01, 0x01, 0x00,
            // start equality filter
            (byte) 0xa3, 0x10,
            // attribute of cn
            0x04, 0x02, 0x43, 0x4e,
            // assertion of John Adams
            0x04, 0x0a, 0x4a, 0x6f, 0x68, 0x6e, 0x20, 0x41, 0x64, 0x61, 0x6d, 0x73,
            // request all user attributes
            0x30, 0x03, 0x04, 0x01, 0x2a,
            // proxy authz control
            (byte) 0xa0, 0x24, 0x30, 0x22,
            // oid
            0x04, 0x18, 0x32, 0x2e, 0x31, 0x36, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x2e, 0x31, 0x31, 0x33, 0x37, 0x33,
            0x30, 0x2e, 0x33, 0x2e, 0x34, 0x2e, 0x31, 0x38,
            // criticality
            0x01, 0x01, (byte) 0xff,
            // value
            0x04, 0x03, 0x64, 0x6e, 0x3a},
        },
      };
  }


  /**
   * @param  request  search request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final SearchRequest request, final byte[] berValue)
    throws Exception
  {
    assertThat(request.encode(2)).isEqualTo(berValue);
  }


  @Test
  public void copy()
  {
    final SearchRequest request = SearchRequest.builder()
      .dn("dc=ldaptive,dc=org")
      .scope(SearchScope.OBJECT)
      .aliases(DerefAliases.ALWAYS)
      .sizeLimit(5)
      .timeLimit(Duration.ofMinutes(1))
      .typesOnly(false)
      .filter("(uid=1234)")
      .returnAttributes("cn", "sn", "jpegPhoto")
      .binaryAttributes("jpegPhoto")
      .controls(new SortRequestControl())
      .responseTimeout(Duration.ofSeconds(3))
      .build();
    assertThat(SearchRequest.copy(request)).isEqualTo(request);
  }
}
