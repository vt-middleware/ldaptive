ConnectionConfig connConfig = ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .build();

SearchDnResolver dnResolver = SearchDnResolver.builder()
  .factory(new DefaultConnectionFactory(connConfig))
  .dn("ou=people,dc=ldaptive,dc=org")
  .filter("uid={user}")
  .build();

SimpleBindAuthenticationHandler authHandler = new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(connConfig));
Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(new AuthenticationRequest("dfisher", new Credential("password")));
if (response.isSuccess()) {
  // authentication succeeded
} else {
  // authentication failed
}
