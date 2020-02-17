ExtendedOperation passModify = ExtendedOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .throwIf(ResultPredicate.NOT_SUCCESS)
  .build();
ExtendedResponse res = passModify.execute(
  new PasswordModifyRequest(
    "uid=dfisher,ou=people,dc=ldaptive,dc=org", "oldPass", "newPass"));
