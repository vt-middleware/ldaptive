SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
SyncReplClient client = new SyncReplClient(cf, false); // false indicates do not persist
SearchRequest request = SearchRequest.objectScopeSearchRequest("dc=ldaptive,dc=org");
client.setOnEntry(e -> {
  // process an entry
  SyncStateControl ssc = (SyncStateControl) e.getControl(SyncStateControl.OID);
});
client.setOnMessage(m -> {
  // process a message
});
client.setOnResult(r -> {
  // synchronization complete
  SyncDoneControl syncDoneControl = (SyncDoneControl) r.getControl(SyncDoneControl.OID);
});
client.setOnException(e -> {
  // handle exception
});
SearchOperationHandle handle = client.send(request, new DefaultCookieManager());
// wait until result is received
handle.await();
client.close();
