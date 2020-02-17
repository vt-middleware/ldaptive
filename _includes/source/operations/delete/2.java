DeleteOperation delete = DeleteOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .throwIf(ResultPredicate.NOT_SUCCESS)
  .build();
delete.execute(DeleteRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .build());
