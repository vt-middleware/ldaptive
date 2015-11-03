Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  ModifyDnOperation modifyDn = new ModifyDnOperation(conn);
  modifyDn.execute(new ModifyDnRequest("uid=dfisher,ou=people,dc=ldaptive,dc=org", "uid=dfisher,ou=robots,dc=ldaptive,dc=org"));
} finally { 
  conn.close();
 }
