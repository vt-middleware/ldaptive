ConnectionConfig connConfig = ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .build();
Authenticator auth = Authenticator.builder()
  .dnResolver(SearchDnResolver.builder()
    .factory(new DefaultConnectionFactory(connConfig))
    .dn("ou=people,dc=ldaptive,dc=org")
    .filter("uid={user}")
    .build())
  .authenticationHandler(new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(connConfig)))
  .responseHandlers(new FreeIPAAuthenticationResponseHandler())
  .attributes(FreeIPAAuthenticationResponseHandler.ATTRIBUTES)
  .build();
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
