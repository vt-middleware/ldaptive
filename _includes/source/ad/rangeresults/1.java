Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  SearchOperation search = new SearchOperation(conn);
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org", "(sn=fisher)");
  // perform additional searches for range attributes
  request.setSearchEntryHandlers(new RangeEntryHandler());
  SearchResult result = search.execute(request).getResult();
  for (LdapEntry entry : result.getEntries()) {
    // do something useful with the entry
  }

} finally { 
  conn.close();
}
