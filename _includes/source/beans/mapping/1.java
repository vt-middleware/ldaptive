ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
SearchOperation search = new SearchOperation(cf, "dc=ldaptive,dc=org");
SearchResponse res = search.execute("(uid=dfisher)");
LdapEntry entry = res.getEntry();

DefaultLdapEntryMapper<MyObject> mapper = new DefaultLdapEntryMapper<>();
MyObject object = new MyObject();
mapper.map(entry, object); // object is now populated with the values from the ldap entry

LdapEntry newEntry = new LdapEntry();
mapper.map(object, newEntry); // newEntry is now populated with the values from the object
