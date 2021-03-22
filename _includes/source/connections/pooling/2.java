PooledConnectionFactory cf = PooledConnectionFactory.builder()
  .config(ConnectionConfig.builder()
    .url("ldap://directory.ldaptive.org")
    .build())
  .validatePeriodically(true)
  .validator(new CompareConnectionValidator(CompareRequest.builder()
    .dn("ou=people,dc=vt,dc=edu")
    .name("ou")
    .value("people")
    .build()))
  .build();
  cf.initialize();
try {
  SearchOperation search = new SearchOperation(cf, "dc=ldaptive,dc=org");
  SearchResponse response = search.execute("(uid=dfisher)");
  LdapEntry entry = response.getEntry();
} finally {
  // close the pool
  cf.close();
}
