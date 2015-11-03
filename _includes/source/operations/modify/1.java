Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  ModifyOperation modify = new ModifyOperation(conn);
  // add the mail attribute, remove the sn attribute, change the displayName attribute
  modify.execute(
    new ModifyRequest(
      "uid=dfisher,ou=people,dc=ldaptive,dc=org",
      new AttributeModification(AttributeModificationType.ADD, new LdapAttribute("mail", "dfisher@ldaptive.org")),
      new AttributeModification(AttributeModificationType.REMOVE, new LdapAttribute("sn")),
      new AttributeModification(AttributeModificationType.REPLACE, new LdapAttribute("displayName", "Daniel Fisher"))));
} finally { 
  conn.close();
}
