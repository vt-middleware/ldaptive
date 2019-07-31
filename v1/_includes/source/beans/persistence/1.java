DefaultLdapEntryManager<MyObject> manager = new DefaultLdapEntryManager<MyObject>(
  new DefaultLdapEntryMapper<MyObject>(), new DefaultConnectionFactory("ldap://directory.ldaptive.org"));

// add a new entry to the LDAP
MyObject addObject = new MyObject("uid=dfisher,ou=people,dc=ldaptive,dc=org");
addObject.setName("Daniel Fisher");
addObject.setEmail("dfisher@ldaptive.org");
manager.add(addObject);

// modify the entry in the LDAP
MyObject mergeObject = manager.find(new MyObject("uid=dfisher,ou=people,dc=ldaptive,dc=org"));
mergeObject.setPhoneNumber("555-555-1234");
manager.merge(mergeObject);
