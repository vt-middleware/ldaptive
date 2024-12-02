SearchOperation search = SearchOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .onEntry(entry -> {
    // process the entry
    String uid = entry.getAttribute("uid").getStringValue();
    // if your application is memory constrained, you can return null here
    // in that case the entry will not be availble in the search response
    // but presumably you no longer need a reference to it
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
  .returnAttributes("uid")
  .build());
