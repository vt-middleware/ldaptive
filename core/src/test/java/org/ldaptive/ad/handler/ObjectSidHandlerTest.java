/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link ObjectSidHandler}.
 *
 * @author  Middleware Services
 */
public class ObjectSidHandlerTest
{


  /**
   * Entry test data.
   *
   * @return  test data
   */
  @DataProvider(name = "entries")
  public Object[][] createEntries()
  {
    return
      new Object[][] {
        new Object[] {
          new ObjectSidHandler(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder()
                .name("objectSid")
                .values(LdapUtils.base64Decode("AQUAAAAAAAUVAAAA1XinPvtHrNQJiIhkUQQAAA=="))
                .binary(true)
                .build())
            .build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder()
                .name("objectSid")
                .values("S-1-5-21-1051162837-3568060411-1686669321-1105")
                .build())
            .build(),
        },
      };
  }


  /**
   * @param  handler  to test
   * @param  actual  to handle
   * @param  expected  to compare
   */
  @Test(groups = "handler", dataProvider = "entries")
  public void apply(final ObjectSidHandler handler, final LdapEntry actual, final LdapEntry expected)
  {
    assertThat(handler.apply(actual)).isEqualTo(expected);
  }


  @Test
  public void setRequest()
  {
    final ObjectSidHandler handler = new ObjectSidHandler();
    handler.setRequest(SearchRequest.builder().build());
    assertThat(handler.getRequest().getBinaryAttributes()).isEqualTo(new String[] {"objectSid"});

    handler.setRequest(SearchRequest.builder().binaryAttributes("jpegPhoto").build());
    assertThat(handler.getRequest().getBinaryAttributes()).isEqualTo(new String[] {"jpegPhoto", "objectSid"});
  }


  @Test
  public void newInstance()
  {
    ObjectSidHandler handler = new ObjectSidHandler();
    assertThat(handler.newInstance()).isEqualTo(handler);

    handler = new ObjectSidHandler("foo");
    assertThat(handler.newInstance()).isEqualTo(handler);
  }
}
