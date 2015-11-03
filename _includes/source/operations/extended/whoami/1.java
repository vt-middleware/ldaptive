Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try {
  conn.open();
  WhoAmIOperation whoami = new WhoAmIOperation(conn);
  Response<String> response = whoami.execute(new WhoAmIRequest());
  String authzId = response.getResult();
} finally {
  conn.close();
}
