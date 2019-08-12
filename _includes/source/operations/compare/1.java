CompareOperation compare = new CompareOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
CompareResponse res = compare.execute(CompareRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .name("mail")
  .value("dfisher@ldaptive.org")
  .build());
if (res.isTrue()) {
  // compare succeeded
} else {
  // compare failed
}
