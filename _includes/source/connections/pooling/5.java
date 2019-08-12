PooledConnectionFactory cf = PooledConnectionFactory.builder()
  .config(ConnectionConfig.builder()
    .url("ldap://directory.ldaptive.org")
    .build())
  .config(PoolConfig.builder()
    .validatePeriodically(true)
    .build())
  .pruneStrategy(new IdlePruneStrategy(Duration.ofMinutes(15), Duration.ofMinutes(30)))
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
