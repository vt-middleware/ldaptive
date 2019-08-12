SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchRequest request = SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(givenName=d*)")
  .attributes("cn", "sn")
  .controls(new SortRequestControl(new SortKey[] {new SortKey("sn", "caseExactMatch")}, true)) // sort by surname
  .build();
SearchResponse response = search.execute(request);
for (LdapEntry entry : response.getEntries()) {
  // do something useful with the entry
}
