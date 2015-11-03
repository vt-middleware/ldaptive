Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  BindOperation bind = new BindOperation(conn);
  bind.execute(new BindRequest("uid=dfisher,ou=people,dc=ldaptive,dc=org", new Credential("password")));
  // perform another operation as this principal
} finally { 
  conn.close();
 }
