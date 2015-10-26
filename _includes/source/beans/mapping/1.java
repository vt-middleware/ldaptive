ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
SearchExecutor executor = new SearchExecutor();
executor.setBaseDn("dc=ldaptive,dc=org");
SearchResult result = executor.search(cf, "(uid=dfisher)").getResult();
LdapEntry entry = result.getEntry();

DefaultLdapEntryMapper mapper = new DefaultLdapEntryMapper();
MyObject object = new MyObject();
mapper.map(entry, object); // object is now populated with the values from the ldap entry

LdapEntry newEntry = new LdapEntry();
mapper.map(object, newEntry); // newEntry is now populated with the values from the object
