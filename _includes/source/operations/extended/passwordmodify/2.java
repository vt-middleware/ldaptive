ExtendedOperation passModify = new ExtendedOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
ExtendedResponse response = passModify.execute(new PasswordModifyRequest("uid=dfisher,ou=people,dc=ldaptive,dc=org"));
String genPass = PasswordModifyResponseParser.parse(response);
