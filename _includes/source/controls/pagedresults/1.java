Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  PagedResultsClient client = new PagedResultsClient(conn, 25); // return 25 entries at a time
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
  Response<SearchResult> response = client.executeToCompletion(request);
  SearchResult result = response.getResult();
  for (LdapEntry entry : result.getEntries()) {
    // do something useful with the entry
  }
} finally { 
  conn.close();
}
