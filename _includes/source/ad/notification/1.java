Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(new BindRequest("cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("manager_password"))); 
  NotificationClient client = new NotificationClient(conn);
  SearchRequest request = new SearchRequest("ou=people,dc=ldaptive,dc=edu", new SearchFilter("(objectClass=*)"));
  request.setSearchScope(SearchScope.ONELEVEL);
  BlockingQueue<NotificationClient.NotificationItem> results = client.execute(request);
  while (true) { 
    NotificationClient.NotificationItem item = results.take(); // blocks until result is received
    if (item.isEntry()) {
      LdapEntry entry = item.getEntry();
    } else if (item.isException()) {
      break;
    }
  }
} finally { 
  conn.close();
}
