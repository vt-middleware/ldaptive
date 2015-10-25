Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  VirtualListViewClient client = new VirtualListViewClient(
    conn, new SortKey[] {
      new SortKey("uid", "caseExactMatch"),
      new SortKey("givenName", "caseIgnoreMatch")});
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
  Response<SearchResult> response = client.execute(request, new VirtualListViewParams(0, 0, 4)); // get the first 5 entries
  // examine the response and then execute another search
  response = client.execute(request, new VirtualListViewParams(5, 0, 4), response); // get the next 5 entries
} finally { 
  conn.close();
}
