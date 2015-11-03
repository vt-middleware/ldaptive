Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn);
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
  SortRequestControl src = new SortRequestControl(new SortKey[] {new SortKey("sn", "caseExactMatch")}, true); // sort by surname
  request.setControls(src);
  SearchResult result = search.execute(request).getResult();
  for (LdapEntry entry : result.getEntries()) {
    // do something useful with the entry
  }

} finally {
  conn.close();
}
