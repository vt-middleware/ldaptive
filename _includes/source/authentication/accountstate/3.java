ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
dnResolver.setBaseDn("ou=people,dc=ldaptive,dc=org");
dnResolver.setUserFilter("uid={user}");
BindAuthenticationHandler authHandler = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig));
Authenticator auth = new Authenticator(dnResolver, authHandler);
auth.setAuthenticationResponseHandlers(new EDirectoryAuthenticationResponseHandler());
AuthenticationResponse response = auth.authenticate(
  new AuthenticationRequest(
    "dfisher", new Credential("password"), new String[] {"passwordExpirationTime", "loginGraceRemaining"}));
if (response.getResult()) {
  // authentication succeeded, check account state
  AccountState state = response.getAccountState();
  // authentication succeeded, only a warning should exist
  AccountState.Warning warning = state.getWarning();
} else {
  // authentication failed, check account state
  AccountState state = response.getAccountState();
  // authentication failed, only an error should exist
  AccountState.Error error = state.getError();
}
