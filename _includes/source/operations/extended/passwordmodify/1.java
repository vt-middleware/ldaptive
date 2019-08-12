ExtendedOperation passModify = new ExtendedOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
passModify.execute(
  new PasswordModifyRequest(
    "uid=dfisher,ou=people,dc=ldaptive,dc=org", "oldPass", "newPass"));
