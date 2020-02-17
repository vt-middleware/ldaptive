ModifyDnOperation modifyDn = new ModifyDnOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
ModifyDnResponse res = modifyDn.execute(ModifyDnRequest.builder()
  .oldDN("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .newRDN("uid=danielf")
  .delete(true)
  .build());
if (res.isSuccess()) {
  // modify succeeded
} else {
  // modify failed
}
