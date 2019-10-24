DefaultConnectionFactory.builder()
  .config(ConnectionConfig.builder()
    .url("ldap://directory.ldaptive.org")
    .connectTimeout(Duration.ofSeconds(5))
    .responseTimeout(Duration.ofSeconds(5))
    .build())
  .build();