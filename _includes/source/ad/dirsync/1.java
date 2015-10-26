Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(new BindRequest("cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("manager_password"))); 
  DirSyncClient client = new DirSyncClient(
    conn, new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, });
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org", "(uid=*)");
  Response<SearchResult> response = client.executeToCompletion(request, new DefaultCookieManager());
  for (LdapEntry entry : response.getResult().getEntries()) {
    // do something useful with the entry
  }
} finally { 
  conn.close();
}
