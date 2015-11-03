Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchRequest request1 = new SearchRequest("dc=ldaptive,dc=org","(givenName=daniel)");
  SearchRequest request2 = new SearchRequest("dc=ldaptive,dc=org","(sn=fisher)");
  SearchOperationWorker search = new SearchOperationWorker(new SearchOperation(conn));

  // to perform a single search
  Future<Response<SearchResult>> future = search.execute(request1);

  // to perform multiple searches
  Collection<Future<Response<SearchResult>>> futures = search.execute(request1, request2);

  // to perform multiple searches and wait
  Collection<Response<SearchResult>> responses = search.executeToCompletion(request1, request2);

  // cleanup the underlying executor service
  search.shutdown();
} finally {
  conn.close();
}
