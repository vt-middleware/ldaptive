Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  DeleteOperation delete = new DeleteOperation(conn);
  delete.execute(new DeleteRequest("uid=dfisher,ou=people,dc=ldaptive,dc=org"));
} finally { 
  conn.close();
 }
