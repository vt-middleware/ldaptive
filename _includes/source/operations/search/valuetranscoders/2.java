Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  SearchOperation search = new SearchOperation(conn);
  SearchResult result = search.execute(
    new SearchRequest(
      "dc=ldaptive,dc=org","(&(givenName=daniel)(sn=fisher))", "modifyTimestamp")).getResult();
  LdapEntry entry = result.getEntry();
  Calendar modifyTimestamp = entry.getAttribute("modifyTimestamp").getValue(new GeneralizedTimeValueTranscoder());

} finally { 
  conn.close();
}
