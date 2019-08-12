// use a format dn resolver
FormatDnResolver dnResolver = new FormatDnResolver("%s@ldaptive.org");

// use a pooled bind authentication handler
PooledConnectionFactory factory = new PooledConnectionFactory(ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .connectionInitializer(new FastBindConnectionInitializer())
  .build());
factory.initialize();

SimpleBindAuthenticationHandler authHandler = new SimpleBindAuthenticationHandler(factory);
Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(
  new AuthenticationRequest("dfisher", new Credential("password")));
if (response.isSuccess()) { // authentication succeeded

} else { // authentication failed
  String msg = response.getDiagnosticMessage(); // read the failure message
}
