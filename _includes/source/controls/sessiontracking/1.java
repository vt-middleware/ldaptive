Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  BindOperation bind = new BindOperation(conn);
  BindRequest request = new BindRequest(
    "uid=dfisher,ou=people,dc=ldaptive,dc=org", new Credential("password"));
  request.setControls(
    new SessionTrackingControl(
      "151.101.32.133", // client IP address
      "hostname.domain.com", // client host name, empty string if unknown
      SessionTrackingControl.USERNAME_ACCT_OID, // must assign an OID even if using an empty identifier
      "")); // empty tracking identifier
  boolean result = bind.execute(request).getResult();
} finally {
  conn.close();
}
