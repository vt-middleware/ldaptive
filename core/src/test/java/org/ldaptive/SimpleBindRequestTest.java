/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import org.ldaptive.control.PasswordPolicyControl;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SimpleBindRequest}.
 *
 * @author  Middleware Services
 */
public class SimpleBindRequestTest
{


  /**
   * Simple bind test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[]{
          SimpleBindRequest.builder()
            .dn("uid=jdoe,ou=People,dc=example,dc=com")
            .password("secret123").build(),
          new byte[]{
            // preamble
            0x30, 0x39, 0x02, 0x01, 0x01, 0x60, 0x34, 0x02, 0x01, 0x03,
            // bind dn
            0x04, 0x24, 0x75, 0x69, 0x64, 0x3d, 0x6a, 0x64, 0x6f, 0x65, 0x2c, 0x6f, 0x75, 0x3d, 0x50, 0x65, 0x6f, 0x70,
            0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d,
            0x63, 0x6f, 0x6d,
            // password
            (byte) 0x80, 0x09, 0x73, 0x65, 0x63, 0x72, 0x65, 0x74, 0x31, 0x32, 0x33},
        },
        new Object[]{
          SimpleBindRequest.builder()
            .dn("uid=jdoe,ou=People,dc=example,dc=com")
            .password("secret123")
            .controls(new PasswordPolicyControl()).build(),
          new byte[]{
            // preamble
            0x30, 0x5b, 0x02, 0x01, 0x01, 0x60, 0x34, 0x02, 0x01, 0x03,
            // bind dn
            0x04, 0x24, 0x75, 0x69, 0x64, 0x3d, 0x6a, 0x64, 0x6f, 0x65, 0x2c, 0x6f, 0x75, 0x3d, 0x50, 0x65, 0x6f,
            0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63,
            0x3d, 0x63, 0x6f, 0x6d,
            // password
            (byte) 0x80, 0x09, 0x73, 0x65, 0x63, 0x72, 0x65, 0x74, 0x31, 0x32, 0x33,
            // ppolicy control
            (byte) 0xa0, 0x20, 0x30, 0x1e,
            0x04, 0x19, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x34, 0x32, 0x2e,
            0x32, 0x2e, 0x32, 0x37, 0x2e, 0x38, 0x2e, 0x35, 0x2e, 0x31,
            0x01, 0x01, 0x00},
        },
      };
  }


  /**
   * @param  request  simple bind request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final SimpleBindRequest request, final byte[] berValue)
    throws Exception
  {
    assertThat(request.encode(1)).isEqualTo(berValue);
  }


  @Test
  public void nullOrEmptyDn()
  {
    try {
      new SimpleBindRequest(null, "pass");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().dn(null).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("", "pass");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().dn("").build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }


  @Test
  public void nullOrEmptyPassword()
  {
    try {
      new SimpleBindRequest("dn", (String) null);
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password((String) null).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("dn", "");
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password("").build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }


  @Test
  public void nullOrEmptyCredential()
  {
    try {
      new SimpleBindRequest("dn", new Credential((String) null));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("dn", new Credential((char[]) null));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("dn", new Credential((byte[]) null));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password(new Credential((String) null)).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password(new Credential((char[]) null)).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password(new Credential((byte[]) null)).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("dn", new Credential(""));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("dn", new Credential("".toCharArray()));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      new SimpleBindRequest("dn", new Credential("".getBytes(StandardCharsets.UTF_8)));
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password(new Credential("")).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password(new Credential("".toCharArray())).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
    try {
      SimpleBindRequest.builder().password(new Credential("".getBytes(StandardCharsets.UTF_8))).build();
      fail("Should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }
}
