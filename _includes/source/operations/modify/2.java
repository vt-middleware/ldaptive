ModifyOperation modify = ModifyOperation.builder()
  .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
  .throwIf(ResultPredicate.NOT_SUCCESS)
  .build();
modify.execute(ModifyRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .modificiations(
  new AttributeModification(AttributeModification.Type.ADD, new LdapAttribute("mail", "dfisher@ldaptive.org")),
  new AttributeModification(AttributeModification.Type.DELETE, new LdapAttribute("sn")),
  new AttributeModification(AttributeModification.Type.REPLACE, new LdapAttribute("displayName", "Daniel Fisher")))
  .build());
