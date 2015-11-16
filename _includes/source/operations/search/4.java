// create a cache with size=50, timeToLive=600 (seconds), interval=300 (seconds)
LRUCache<SearchRequest> cache = new LRUCache<SearchRequest>(50, 600, 300);
Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn, cache);
  SearchResult result = search.execute(new SearchRequest("dc=ldaptive,dc=org", "(uid=dfisher)")).getResult();
} finally {
  conn.close();
}
