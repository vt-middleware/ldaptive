Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  SearchOperation search = new SearchOperation(conn);
  SearchResult result = search.execute(
    new SearchRequest(
      "dc=ldaptive,dc=org","(&(givenName=daniel)(sn=fisher))", "userCertificate;binary")).getResult();
  LdapEntry entry = result.getEntry();
  Certificate cert = entry.getAttribute("userCertificate;binary").getValue(new CertificateValueTranscoder());

} finally { 
  conn.close();
}
