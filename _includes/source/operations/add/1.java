AddOperation add = new AddOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
AddResponse res = add.execute(AddRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .attributes(new LdapAttribute("uid", "dfisher"), new LdapAttribute("mail", "dfisher@ldaptive.org"))
  .build());
if (res.isSuccess()) {
  // add succeeded
} else {
  // add failed
}
