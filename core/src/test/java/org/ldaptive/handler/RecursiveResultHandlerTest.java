/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link RecursiveResultHandler}.
 *
 * @author  Middleware Services
 */
public class RecursiveResultHandlerTest
{


  @Test(groups = "handlers")
  public void apply()
  {
    final LdapEntry parent = LdapEntry.builder().dn("uid=parent,ou=groups,dc=ldaptive,dc=org").attributes(
      LdapAttribute.builder().name("uid").values("parent").build(),
      LdapAttribute.builder().name("member").values("uid=child1,ou=groups,dc=ldaptive,dc=org").build()).build();

    final LdapEntry child1 = LdapEntry.builder().dn("uid=child1,ou=groups,dc=ldaptive,dc=org").attributes(
      LdapAttribute.builder().name("uid").values("child1").build(),
      LdapAttribute.builder().name("member").values(
        "uid=child2,ou=groups,dc=ldaptive,dc=org", "uid=child3,ou=groups,dc=ldaptive,dc=org").build()).build();

    final LdapEntry child2 = LdapEntry.builder().dn("uid=child2,ou=groups,dc=ldaptive,dc=org").attributes(
      LdapAttribute.builder().name("uid").values("child2").build()).build();

    final LdapEntry child3 = LdapEntry.builder().dn("uid=child3,ou=groups,dc=ldaptive,dc=org").attributes(
      LdapAttribute.builder().name("uid").values("child3").build(),
      LdapAttribute.builder().name("member").values("uid=child4,ou=groups,dc=ldaptive,dc=org").build()).build();

    final LdapEntry child4 = LdapEntry.builder().dn("uid=child4,ou=groups,dc=ldaptive,dc=org").attributes(
      LdapAttribute.builder().name("uid").values("child4").build()).build();

    final TestRecursiveResultHandler handler = new TestRecursiveResultHandler("member", "uid");
    handler.setEntries(
      Map.of("uid=child1,ou=groups,dc=ldaptive,dc=org", child1,
             "uid=child2,ou=groups,dc=ldaptive,dc=org", child2,
             "uid=child3,ou=groups,dc=ldaptive,dc=org", child3,
             "uid=child4,ou=groups,dc=ldaptive,dc=org", child4));

    final LdapEntry expected = LdapEntry.builder().dn("uid=parent,ou=groups,dc=ldaptive,dc=org").attributes(
      LdapAttribute.builder().name("uid").values("parent", "child1", "child2", "child3", "child4").build(),
      LdapAttribute.builder().name("member").values("uid=child1,ou=groups,dc=ldaptive,dc=org").build()).build();

    final SearchResponse response = SearchResponse.builder().entry(parent).build();
    Assert.assertEquals(handler.apply(response).getEntry(), expected);
  }


  /** Class for testing {@link RecursiveResultHandler}. */
  private static class TestRecursiveResultHandler extends RecursiveResultHandler
  {

    /** Entries to return. */
    private final Map<String, LdapEntry> entries = new HashMap<>();


    /**
     * Test instance of {@link RecursiveResultHandler}.
     *
     * @param  searchAttr  attribute to search on
     * @param  mergeAttrs  attribute names to merge
     */
    TestRecursiveResultHandler(final String searchAttr, final String... mergeAttrs)
    {
      super(searchAttr, mergeAttrs);
    }


    void setEntries(final Map<String, LdapEntry> results)
    {
      entries.putAll(results);
    }


    @Override
    protected SearchResponse performSearch(final String baseDn, final String[] retAttrs)
      throws LdapException
    {
      if (!entries.containsKey(baseDn)) {
        return SearchResponse.builder().resultCode(ResultCode.SUCCESS).build();
      }
      return SearchResponse.builder().entry(entries.get(baseDn)).resultCode(ResultCode.SUCCESS).build();
    }
  }
}
