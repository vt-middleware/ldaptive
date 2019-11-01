SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
  SearchResponse res = search.execute(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(&(givenName=daniel)(sn=fisher))")
  .returnAttributes("mail", "displayName")
  .build());
SearchResponse sortedRes = SearchResponse.sort(res);
