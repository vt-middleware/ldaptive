Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  // only return changed entries, return the entry change control
  PersistentSearchClient client = new PersistentSearchClient(conn, EnumSet.allOf(PersistentSearchChangeType.class), true, true);
  SearchRequest request = SearchRequest.newObjectScopeSearchRequest("dc=ldaptive,dc=org");
  BlockingQueue<PersistentSearchItem> results = client.execute(request);
  while (true) {
    PersistentSearchItem item = results.take(); // blocks until result is received
    if (item.isEntry()) {
      EntryChangeNotificationControl nc = item.getEntry().getEntryChangeNotificationControl();
      LdapEntry entry = item.getEntry().getSearchEntry();
      // process this entry with the entry change control data
    } else if (item.isResponse()) {
      // response received
      break;
    } else if (item.isAsyncRequest()) {
      // request has begun
      AsyncRequest ar = item.getAsyncRequest();
    } else if (item.isException()) {
      // an error has occurred
      throw item.getException();
    }
  }
} finally {
  conn.close();
}
