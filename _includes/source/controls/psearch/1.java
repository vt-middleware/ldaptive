ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
// only return changed entries, return the entry change control
PersistentSearchClient client = new PersistentSearchClient(cf, EnumSet.allOf(PersistentSearchChangeType.class), true, true);
SearchRequest request = SearchRequest.objectScopeSearchRequest("dc=ldaptive,dc=org");
BlockingQueue<PersistentSearchItem> results = client.execute(request);
while (true) {
  PersistentSearchItem item = results.take(); // blocks until result is received
  if (item.isEntry()) {
    EntryChangeNotificationControl nc = item.getEntry().getEntryChangeNotificationControl();
    LdapEntry entry = item.getEntry().getSearchEntry();
    // process this entry with the entry change control data
  } else if (item.isResult()) {
    // result received
    break;
  } else if (item.isException()) {
    // an error has occurred
    throw item.getException();
  }
}
