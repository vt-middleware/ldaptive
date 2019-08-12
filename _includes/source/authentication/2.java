ConnectionConfig connConfig = ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .connectionInitializer(new BindConnectionInitializer("cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("password")))
  .build();
SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
