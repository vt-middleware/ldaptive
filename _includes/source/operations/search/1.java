Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  SearchOperation search = new SearchOperation(conn);
  SearchResult result = search.execute(
    new SearchRequest(
      "dc=ldaptive,dc=org","(&(givenName=daniel)(sn=fisher))", "mail", "displayName")).getResult();
  result.getEntry(); // if you're expecting a single entry
  for (LdapEntry entry : result.getEntries()) { // if you're expecting multiple entries
    // do something useful with the entry
  }

} finally { 
  conn.close();
}
