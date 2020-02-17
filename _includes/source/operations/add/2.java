AddOperation add = AddOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .throwIf(ResultPredicate.NOT_SUCCESS)
  .build();
add.execute(AddRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .attributes(new LdapAttribute("uid", "dfisher"), new LdapAttribute("mail", "dfisher@ldaptive.org"))
  .build());
