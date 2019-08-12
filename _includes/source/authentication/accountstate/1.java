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
authHandler.setAuthenticationControls(new PasswordPolicyControl());
Authenticator auth = new Authenticator(dnResolver, authHandler);
auth.setResponseHandlers(new PasswordPolicyAuthenticationResponseHandler());
AuthenticationResponse response = auth.authenticate(new AuthenticationRequest("dfisher", new Credential("password")));
if (response.isSuccess()) {
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
