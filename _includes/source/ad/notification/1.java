SingleConnectionFactory factory = new SingleConnectionFactory(ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .connectionInitializer(
    new BindConnectionInitializer("cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("manager_password")))
  .build());
factory.initialize();
NotificationClient client = new NotificationClient(factory);
SearchRequest request = SearchRequest.builder()
  .dn("ou=people,dc=ldaptive,dc=edu")
  .filter("(objectClass=*)")
  .scope(SearchScope.ONELEVEL)
  .build();
BlockingQueue<NotificationClient.NotificationItem> results = client.execute(request);
while (true) {
  NotificationClient.NotificationItem item = results.take(); // blocks until result is received
  if (item.isEntry()) {
    LdapEntry entry = item.getEntry();
  } else if (item.isException()) {
    break;
  }
}
factory.close();
