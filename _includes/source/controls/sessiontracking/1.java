BindOperation bind = new BindOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
BindRequest request = SimpleBindRequest.builder()
  .dn("uid=dfisher,ou=people,dc=ldaptive,dc=org")
  .password("password")
  .controls(new SessionTrackingControl(
    "151.101.32.133", // client IP address
    "hostname.domain.com", // client host name, empty string if unknown
    SessionTrackingControl.USERNAME_ACCT_OID, // must assign an OID even if using an empty identifier
    "")) // empty tracking identifier
  .build();
bind.execute(request);
