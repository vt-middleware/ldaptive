SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchRequest request = SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(givenName=d*)")
  .returnAttributes("cn", "sn")
  .controls(new ManageDsaITControl())
  .build();
SearchResponse res = search.execute(request);
for (LdapEntry entry : res.getEntries()) {
  // do something useful with the entry
}
