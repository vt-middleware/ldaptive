SingleConnectionFactory cf = new SingleConnectionFactory(("ldap://directory.ldaptive.org");
cf.initialize();
SyncReplClient client = new SyncReplClient(cf, true); // true indicates persist
SearchRequest request = SearchRequest.objectScopeSearchRequest("dc=ldaptive,dc=org");
BlockingQueue<SyncReplItem> results = client.execute(request, new DefaultCookieManager());
while (true) {
  SyncReplItem item = results.take(); // blocks until result is received
  if (item.isEntry()) {
    SyncStateControl ssc = item.getEntry().getSyncStateControl();
    LdapEntry entry = item.getEntry().getSearchEntry();
    // process this entry with the sync state control data
    if (entry.size() > 0) { // arbitrary condition
      // stop receiving updates
      client.cancel();
    }
  } else if (item.isMessage()) {
    SyncInfoMessage sim = item.getMessage();
    // process this info message
  } else if (item.isResult()) {
    SyncDoneControl sdc = item.getResult().getSyncDoneControl();
    // synchronization complete
    break;
  } else if (item.isException()) {
    // an error has occurred
    throw item.getException();
  }
}
cf.close();
