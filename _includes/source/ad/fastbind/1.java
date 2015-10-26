// use a format dn resolver
FormatDnResolver dnResolver = new FormatDnResolver("%s@ldaptive.org");

// use a pooled bind authentication handler
ConnectionConfig handlerConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
handlerConfig.setUseStartTLS(true);
handlerConfig.setConnectionInitializer(new FastBindOperation.FastBindConnectionInitializer());
BlockingConnectionPool handlerPool = new BlockingConnectionPool(new DefaultConnectionFactory(handlerConfig));
handlerPool.initialize();

PooledBindAuthenticationHandler authHandler = new PooledBindAuthenticationHandler(new PooledConnectionFactory(handlerPool));

Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(
  new AuthenticationRequest("dfisher", new Credential("password")));
if (response.getResult()) { // authentication succeeded
  ...
} else { // authentication failed
  String msg = response.getMessage(); // read the failure message
}
