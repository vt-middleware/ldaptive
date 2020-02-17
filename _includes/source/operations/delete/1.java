DeleteOperation delete = new DeleteOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
DeleteResponse res = delete.execute(DeleteRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .build());
if (res.isSuccess()) {
  // delete succeeded
} else {
  // delete failed
}
