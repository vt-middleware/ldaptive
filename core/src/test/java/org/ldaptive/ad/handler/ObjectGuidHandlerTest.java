/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchRequest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ObjectGuidHandler}.
 *
 * @author  Middleware Services
 */
public class ObjectGuidHandlerTest
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
          new ObjectGuidHandler(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder()
                .name("objectGUID")
                .values(LdapUtils.base64Decode("yjzbsb1yMU+ev8cM1EvaMg=="))
                .binary(true)
                .build())
            .build(),
          LdapEntry.builder()
            .dn("cn=Homer Simpson,ou=People,dc=ldaptive,dc=org")
            .attributes(
              LdapAttribute.builder().name("cn").values("Homer").build(),
              LdapAttribute.builder().name("sn").values("Simpson").build(),
              LdapAttribute.builder()
                .name("objectGUID")
                .values("{B1DB3CCA-72BD-4F31-9EBF-C70CD44BDA32}")
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
  public void apply(final ObjectGuidHandler handler, final LdapEntry actual, final LdapEntry expected)
  {
    Assert.assertEquals(handler.apply(actual), expected);
  }

  @Test
  public void setRequest()
  {
    final ObjectGuidHandler handler = new ObjectGuidHandler();
    handler.setRequest(SearchRequest.builder().build());
    Assert.assertEquals(handler.getRequest().getBinaryAttributes(), new String[] {"objectGUID"});

    handler.setRequest(SearchRequest.builder().binaryAttributes("jpegPhoto").build());
    Assert.assertEquals(handler.getRequest().getBinaryAttributes(), new String[] {"jpegPhoto", "objectGUID"});
  }
}
