SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
PagedResultsClient client = new PagedResultsClient(cf, 25); // return 25 entries at a time
SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
SearchResponse response = client.executeToCompletion(request);
for (LdapEntry entry : response.getEntries()) {
  // do something useful with the entry
}
cf.close();
