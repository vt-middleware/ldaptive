Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn);
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("(givenName=daniel)"));
  request.setControls(
    new SessionTrackingControl(
      "151.101.32.133", // client IP address
      "hostname.domain.com", // client host name, empty string if unknown
      SessionTrackingControl.USERNAME_ACCT_OID,
      "dn:uid=dfisher,ou=people,dc=ldaptive,dc=org"));
  SearchResult result = search.execute(request).getResult();
  for (LdapEntry entry : result.getEntries()) {
    // do something useful with the entries
  }
} finally {
  conn.close();
}
