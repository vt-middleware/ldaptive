ModifyOperation modify = new ModifyOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
ModifyResponse res = modify.execute(ModifyRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .modifications(
  new AttributeModification(AttributeModification.Type.ADD, new LdapAttribute("mail", "dfisher@ldaptive.org")),
  new AttributeModification(AttributeModification.Type.DELETE, new LdapAttribute("sn")),
  new AttributeModification(AttributeModification.Type.REPLACE, new LdapAttribute("displayName", "Daniel Fisher")))
  .build());
if (res.isSuccess()) {
  // add succeeded
} else {
  // add failed
}
