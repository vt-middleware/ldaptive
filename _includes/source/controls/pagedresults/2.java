Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  PagedResultsClient client = new PagedResultsClient(conn, 25); // return 25 entries at a time
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
  Response<SearchResult> response = client.execute(request);
  while (client.hasMore(response)) {
    response = client.execute(request, response);
    // inspect the response and break out of the loop if necessary
  }
} finally {
  conn.close();
}
