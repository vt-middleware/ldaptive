Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  BindOperation bind = new BindOperation(conn);
  bind.execute(new BindRequest("dfisher@ldaptive.org", new Credential("password"), new DigestMd5Config()));
  // perform another operation as this principal
} finally { 
  conn.close();
}
