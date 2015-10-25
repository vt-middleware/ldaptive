Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  PasswordModifyOperation modify = new PasswordModifyOperation(conn);
  modify.execute(new PasswordModifyRequest("uid=dfisher,ou=people,dc=ldaptive,dc=org", new Credential("oldPass"), new Credential("newPass")));
} finally {
  conn.close();
}
