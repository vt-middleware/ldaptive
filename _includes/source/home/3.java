SearchOperation search = new SearchOperation(
  DefaultConnectionFactory.builder()
    .config(ConnectionConfig.builder()
      .url("ldap://directory.ldaptive.org")
      .useStartTLS(true)
      .connectionInitializer(BindConnectionInitializer.builder()
        .dn("cn=manager,ou=people,dc=ldaptive,dc=org")
        .credential("manager_password")
        .build())
      .build())
    .build(),
  "dc=ldaptive,dc=org");
SearchResponse response = search.execute("(uid=*fisher)", "mail", "sn");
for (LdapEntry entry : response.getEntries()) {
  // do something useful with the entry
}
