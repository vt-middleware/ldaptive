/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResponse;
import org.ldaptive.SearchResultReference;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link FreezeResultHandler}.
 *
 * @author  Middleware Services
 */
public class FreezeResultHandlerTest
{


  @Test
  public void apply()
  {
    final FreezeResultHandler handler = new FreezeResultHandler();
    final LdapEntry le1 = LdapEntry.builder()
      .dn("cn=test")
      .attributes(LdapAttribute.builder().name("cn").values("test").build())
      .build();
    final SearchResultReference ref1 = SearchResultReference.builder().uris("ldap://ds1.ldaptive.org").build();
    final SearchResponse response = SearchResponse.builder()
      .entry(le1)
      .reference(ref1)
      .build();
    handler.apply(response);
    try {
      response.removeEntries(le1);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.removeReferences(ref1);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.addEntries(LdapEntry.builder().build());
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
    try {
      response.addReferences(SearchResultReference.builder().build());
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalStateException.class);
    }
  }
}
