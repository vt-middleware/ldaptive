SearchOperation search = new SearchOperation(
  new DefaultConnectionFactory("ldap://directory.ldaptive.org"), "dc=ldaptive,dc=org");
SearchResponse response = search.execute("(uid=dfisher)");
LdapEntry entry = response.getEntry();
// do something useful with the entry
