Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SyncReplClient client = new SyncReplClient(conn, false); // false indicates do not persist
  SearchRequest request = SearchRequest.newObjectScopeSearchRequest("dc=ldaptive,dc=org");
  BlockingQueue<SyncReplItem> results = client.execute(request, new DefaultCookieManager());
  while (true) {
    SyncReplItem item = results.take(); // blocks until result is received
    if (item.isEntry()) {
      SyncStateControl ssc = item.getEntry().getSyncStateControl();
      LdapEntry entry = item.getEntry().getSearchEntry();
      // process this entry with the sync state control data
    } else if (item.isMessage()) {
      SyncInfoMessage sim = item.getMessage();
      // process this info message
    } else if (item.isResponse()) {
      SyncDoneControl sdc = item.getResponse().getSyncDoneControl();
      // synchronization complete
      break;
    } else if (item.isAsyncRequest()) {
      // request has begun
      AsyncRequest ar = item.getAsyncRequest();
    } else if (item.isException()) {
      // an error has occurred, try again
      while (true) {
        try {
          conn.reopen();
          break;
        } catch (LdapException e) {
          System.err.println(
            "Failed to reopen connection: " + e.getMessage());
          try {
            Thread.sleep(5000);
          } catch (InterruptedException ie) {}
        }
      }
      results = client.execute(request, new DefaultCookieManager());
    }
  }
} finally {
  conn.close();
}
