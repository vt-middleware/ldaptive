AddOperation add = new AddOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
add.execute(AddRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .attributes(new LdapAttribute("uid", "dfisher"), new LdapAttribute("mail", "dfisher@ldaptive.org"))
  .build());
