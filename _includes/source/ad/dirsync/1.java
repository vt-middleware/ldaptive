SingleConnectionFactory factory = new SingleConnectionFactory(ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .connectionInitializers(
  new BindConnectionInitializer("cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("manager_password")))
  .build());
factory.initialize();
DirSyncClient client = new DirSyncClient(
  factory, new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, });
SearchRequest request = new SearchRequest("dc=ldaptive,dc=org", "(uid=*)");
SearchResponse res = client.executeToCompletion(request, new DefaultCookieManager());
for (LdapEntry entry : res.getEntries()) {
  // do something useful with the entry
}
factory.close();
