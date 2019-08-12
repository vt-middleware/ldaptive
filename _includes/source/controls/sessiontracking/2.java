SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchRequest request = SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(givenName=daniel)")
  .controls(new SessionTrackingControl(
    "151.101.32.133", // client IP address
    "hostname.domain.com", // client host name, empty string if unknown
    SessionTrackingControl.USERNAME_ACCT_OID,
    "dn:uid=dfisher,ou=people,dc=ldaptive,dc=org"))
  .build();
SearchResponse response = search.execute(request);
for (LdapEntry entry : response.getEntries()) {
  // do something useful with the entries
}
