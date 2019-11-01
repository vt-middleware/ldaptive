SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchResponse res = search.execute(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(&(givenName=daniel)(sn=fisher))")
  .returnAttributes("modifyTimestamp")
  .build());
LdapEntry entry = res.getEntry();
ZonedDateTime modifyTimestamp = entry.getAttribute("modifyTimestamp").getValue(new GeneralizedTimeValueTranscoder().decoder());
