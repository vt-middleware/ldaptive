ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
connConfig.setConnectionInitializer(
  new BindConnectionInitializer("cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("password")));
SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
