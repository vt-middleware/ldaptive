/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cache;

import org.ldaptive.LdapEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LRUCache}.
 *
 * @author  Middleware Services
 */
public class LRUCacheTest
{

  /** Cache for testing. */
  private final LRUCache<SearchRequest> cache = new LRUCache<>(5, 60, 3);


  /** @throws  Exception  On test failure. */
  @BeforeClass(groups = {"cache"})
  public void initialize()
    throws Exception
  {
    fillCache();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"cache"})
  public void clear()
    throws Exception
  {
    fillCache();
    AssertJUnit.assertEquals(5, cache.size());
    cache.clear();
    AssertJUnit.assertEquals(0, cache.size());
    cache.close();
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"cache"}, threadPoolSize = 5, invocationCount = 100, timeOut = 60000)
  public void get()
    throws Exception
  {
    SearchResult result = cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=3")));
    AssertJUnit.assertEquals(new SearchResult(new LdapEntry("uid=3,ou=test,dc=ldaptive,dc=org")), result);
    result = cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=4")));
    AssertJUnit.assertEquals(new SearchResult(new LdapEntry("uid=4,ou=test,dc=ldaptive,dc=org")), result);
    result = cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=5")));
    AssertJUnit.assertEquals(new SearchResult(new LdapEntry("uid=5,ou=test,dc=ldaptive,dc=org")), result);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"cache"})
  public void put()
    throws Exception
  {
    AssertJUnit.assertEquals(5, cache.size());
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=%s", new Object[] {"101"})),
      new SearchResult(new LdapEntry("uid=101,ou=test,dc=ldaptive,dc=org")));
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=102")),
      new SearchResult(new LdapEntry("uid=102,ou=test,dc=ldaptive,dc=org")));
    AssertJUnit.assertEquals(5, cache.size());

    SearchResult result = cache.get(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=%s", new Object[] {"101"})));
    AssertJUnit.assertEquals(new SearchResult(new LdapEntry("uid=101,ou=test,dc=ldaptive,dc=org")), result);
    result = cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=102")));
    AssertJUnit.assertEquals(new SearchResult(new LdapEntry("uid=102,ou=test,dc=ldaptive,dc=org")), result);
    AssertJUnit.assertNull(cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=1"))));
  }


  /** Fills the cache with data. */
  private void fillCache()
  {
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=1")),
      new SearchResult(new LdapEntry("uid=1,ou=test,dc=ldaptive,dc=org")));
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=2")),
      new SearchResult(new LdapEntry("uid=2,ou=test,dc=ldaptive,dc=org")));
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=3")),
      new SearchResult(new LdapEntry("uid=3,ou=test,dc=ldaptive,dc=org")));
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=4")),
      new SearchResult(new LdapEntry("uid=4,ou=test,dc=ldaptive,dc=org")));
    cache.put(
      new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=5")),
      new SearchResult(new LdapEntry("uid=5,ou=test,dc=ldaptive,dc=org")));
    // ensure uid=1 and uid=2 get evicted first
    cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=3")));
    cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=4")));
    cache.get(new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("uid=5")));
  }
}
