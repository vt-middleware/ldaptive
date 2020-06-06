PooledConnectionFactory cf = PooledConnectionFactory.builder()
  .config(ConnectionConfig.builder()
    .url("ldap://directory.ldaptive.org")
    .connectionInitializers(BindConnectionInitializer.builder()
      .dn("uid=service,ou=services,dc=ldaptive,dc=org")
      .credential("service-password")
      .build())
    .build())
  .min()
  .max()
  .build();
cf.initialize();
// search operation performed as the service user
SearchOperation search = new SearchOperation(cf, "dc=ldaptive,dc=org");
SearchResponse response = search.execute("(uid=dfisher)");
LdapEntry entry = response.getEntry();
cf.close();
