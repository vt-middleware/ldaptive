ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
connConfig.setConnectionInitializer(
  new BindConnectionInitializer(
    "cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("manager_password")));
ConnectionFactory cf = new DefaultConnectionFactory(connConfig);
SearchExecutor executor = new SearchExecutor();
executor.setBaseDn("dc=ldaptive,dc=org");
SearchResult result = executor.search(cf, "(uid=*fisher)", "mail", "sn").getResult();
for (LdapEntry entry : result.getEntries()) {
  // do something useful with the entry
}
