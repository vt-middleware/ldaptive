ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
VirtualListViewClient client = new VirtualListViewClient(
  cf,
  new SortKey[] {
    new SortKey("uid", "caseExactMatch"),
    new SortKey("givenName", "caseIgnoreMatch")});
SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
SearchResponse response = client.execute(request, new VirtualListViewParams(0, 0, 4)); // get the first 5 entries
// examine the response and then execute another search
response = client.execute(request, new VirtualListViewParams(5, 0, 4), response); // get the next 5 entries
