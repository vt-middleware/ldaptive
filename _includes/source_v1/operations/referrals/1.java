Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
SearchRequest request = new SearchRequest("dc=ldaptive,dc=org", "(&(givenName=daniel)(sn=fisher))");
request.setReferralHandler(new SearchReferralHandler());
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn);
  // referrals will be followed to build the response
  SearchResult result = search.execute(request).getResult();
  for (LdapEntry entry : result.getEntries()) {
    // do something useful with the entry
  }

} finally {
  conn.close();
}
