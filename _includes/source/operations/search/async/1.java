final String uid;
SearchOperation search = SearchOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .onEntry(entry -> {
    // process the entry
    uid = entry.getAttribute("uid").getStringValue();
    return entry;
  })
  .onResult(result -> {
    // search is complete
  })
  .build();

// non-blocking search
search.send(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(&(givenName=d*)(sn=f*))")
  .attributes("uid")
  .build());
