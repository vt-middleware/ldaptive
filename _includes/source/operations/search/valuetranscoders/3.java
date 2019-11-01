SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchResponse res = search.execute(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(&(givenName=daniel)(sn=fisher))")
  .returnAttributes("entryUUID")
  .build());
LdapEntry entry = res.getEntry();
UUID modifyTimestamp = entry.getAttribute("entryUUID").getValue(new UUIDValueTranscoder().decoder());
