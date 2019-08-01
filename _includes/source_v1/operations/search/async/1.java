Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  AsyncSearchOperation search = new AsyncSearchOperation(conn);
  SearchRequest request = new SearchRequest("ou=people,dc=ldaptive,dc=org", "(cn=*fisher)");
  FutureResponse<SearchResult> response = search.execute(request);

  // block until response arrives
  SearchResult result = response.getResult();

  // cleanup the underlying executor service
  search.shutdown();

} finally {
  conn.close();
}
