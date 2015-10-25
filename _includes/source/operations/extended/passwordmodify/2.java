Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  PasswordModifyOperation modify = new PasswordModifyOperation(conn);
  Response<Credential> response = modify.execute(new PasswordModifyRequest("uid=dfisher,ou=people,dc=ldaptive,dc=org"));
  Credential genPass = response.getResult();
  ...
} finally {
  conn.close();
}
