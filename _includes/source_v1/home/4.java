ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
dnResolver.setBaseDn("ou=people,dc=ldaptive,dc=org");
dnResolver.setUserFilter("uid={user}");
BindAuthenticationHandler authHandler = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig));
Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(new AuthenticationRequest("dfisher", new Credential("password")));
if (response.getResult()) {
  // authentication succeeded
} else {
  // authentication failed
}
