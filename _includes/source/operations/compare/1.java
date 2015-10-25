Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try {
  conn.open(); 
  CompareOperation compare = new CompareOperation(conn);
  boolean success = compare.execute(
    new CompareRequest(
    "uid=dfisher,ou=people,dc=ldaptive,dc=org", new LdapAttribute("mail", "dfisher@ldaptive.org"))).getResult();
  // do something useful with the result
} finally { 
 conn.close();
 }
