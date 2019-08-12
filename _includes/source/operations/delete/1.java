DeleteOperation delete = new DeleteOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
delete.execute(DeleteRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .build());
