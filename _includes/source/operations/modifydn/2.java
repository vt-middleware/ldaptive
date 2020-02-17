ModifyDnOperation modifyDn = ModifyDnOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .throwIf(ResultPredicate.NOT_SUCCESS)
  .build();
modifyDn.execute(ModifyDnRequest.builder()
  .oldDN("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .newRDN("uid=danielf")
  .delete(true)
  .build());
