ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
SearchExecutor executor = new SearchExecutor();
executor.setBaseDn("dc=ldaptive,dc=org");
SearchResult result = executor.search(cf, "(uid=dfisher)").getResult();
LdapEntry entry = result.getEntry();
// do something useful with the entry
