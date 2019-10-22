ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
// only return changed entries, return the entry change control
PersistentSearchClient client = new PersistentSearchClient(cf, EnumSet.allOf(PersistentSearchChangeType.class), true, true);
SearchRequest request = SearchRequest.objectScopeSearchRequest("dc=ldaptive,dc=org");
client.setOnEntry(e -> {
  // process an entry
  EntryChangeNotificationControl nc = (EntryChangeNotificationControl) e.getControl(
    EntryChangeNotificationControl.OID);
});
client.setOnResult(r -> {
  // search complete
});
client.setOnException(e -> {
  // handle exception
});
SearchOperationHandle handle = client.send(request);
// wait until result is received
handle.await();
