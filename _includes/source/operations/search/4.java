// create a cache with size=50, timeToLive=10min, interval=5min
LRUCache<SearchRequest> cache = new LRUCache<SearchRequest>(50, Duration.ofMinutes(10), Duration.ofMinutes(5));
Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn, cache);
  SearchResult result = search.execute(new SearchRequest("dc=ldaptive,dc=org", "(uid=dfisher)")).getResult();
} finally {
  conn.close();
}
