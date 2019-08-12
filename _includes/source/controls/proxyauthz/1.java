SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
  SearchRequest request = SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(givenName=daniel)")
  .controls(new ProxyAuthorizationControl("dn:uid=dfisher,ou=people,dc=ldaptive,dc=org"))
  .build();
  SearchResponse result = search.execute(request);
  for (LdapEntry entry : result.getEntries()) {
  // do something useful with the entries
  }
