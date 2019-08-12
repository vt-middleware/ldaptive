ConnectionConfig.builder()
  .url("ldap://directory-1.ldaptive.org" "ldap://directory-2.ldaptive.org" "ldap://directory-3.ldaptive.org")
  .useStartTLS(true)
  .connectTimeout(Duration.ofSeconds(5))
  .responseTimeout(Duration.ofSeconds(5))
  .strategy(new RoundRobinConnectionStrategy())
  .build();
