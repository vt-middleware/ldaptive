DefaultConnectionFactory factory = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
SearchOperation search = new SearchOperation(factory);
search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
SearchRequest request = new SearchRequest("dc=ldaptive,dc=org", "(uid=dfisher)");
SearchResponse response = search.execute(request);
for (LdapEntry entry : response.getEntries()) {
  // do something useful with the entry
}
