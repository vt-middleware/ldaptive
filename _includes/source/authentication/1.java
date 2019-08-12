// use a secure connection for authentication
ConnectionConfig connConfig = ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .build();

// use a search dn resolver
SearchDnResolver dnResolver = SearchDnResolver.builder()
  .factory(new DefaultConnectionFactory(connConfig))
  .dn("ou=people,dc=ldaptive,dc=org")
  .filter("(uid={user})")
  .build();

// perform a simple bind for password validation
SimpleBindAuthenticationHandler authHandler = new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(connConfig));

Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(
  new AuthenticationRequest("dfisher", new Credential("password"), new String[] {"mail", "sn"}));
if (response.isSuccess()) { // authentication succeeded
  LdapEntry entry = response.getLdapEntry(); // read mail and sn attributes
} else { // authentication failed
  String msg = response.getDiagnosticMessage(); // read the failure message
  ResponseControl[] ctls = response.getControls(); // read any response controls
}
