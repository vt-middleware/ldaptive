SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
PagedResultsClient client = new PagedResultsClient(cf, 25); // return 25 entries at a time
SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
SearchResponse response = client.execute(request);
while (client.hasMore(response)) {
  response = client.execute(request, response);
  // inspect the response and break out of the loop if necessary
}
cf.close();
