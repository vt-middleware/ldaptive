ConnectionFactory connFactory = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
Connection conn = connFactory.getConnection();
try {
  // open the connection to the ldap
  conn.open();

  // perform an operation on the connection
  ...
} finally {
  // close the connection to the ldap
  conn.close();
}
