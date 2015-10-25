LdapEntry entry = new LdapEntry(
  "uid=dfisher,ou=people,dc=ldaptive,dc=org",
  new LdapAttribute("uid", "dfisher"),
  new LdapAttribute("mail", "dfisher@ldaptive.org"));

Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  AddOperation add = new AddOperation(conn);
  add.execute(new AddRequest(entry.getDn(), entry.getAttributes()));
} finally {
  conn.close();
}
