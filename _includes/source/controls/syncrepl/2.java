SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
SyncReplClient client = new SyncReplClient(cf, true); // true indicates persist
SearchRequest request = SearchRequest.objectScopeSearchRequest("dc=ldaptive,dc=org");
client.setOnEntry(e -> {
  // process this entry with the sync state control data
  SyncStateControl ssc = (SyncStateControl) e.getControl(SyncStateControl.OID);
  if (e.size() > 0) { // arbitrary condition
    // stop receiving updates
    client.cancel();
  }
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
// wait until result is received (or forever)
handle.await();
client.close();
