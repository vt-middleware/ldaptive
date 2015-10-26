// use a secure connection for authentication
ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);

// use a search dn resolver
SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
dnResolver.setBaseDn("ou=people,dc=ldaptive,dc=org");
dnResolver.setUserFilter("(uid={user})");

// perform a bind for password validation
BindAuthenticationHandler authHandler = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig));

Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(
  new AuthenticationRequest("dfisher", new Credential("password"), new String[] {"mail", "sn"}));
if (response.getResult()) { // authentication succeeded
  LdapEntry entry = response.getLdapEntry(); // read mail and sn attributes

} else { // authentication failed
  String msg = response.getMessage(); // read the failure message
  ResponseControl[] ctls = response.getControls(); // read any response controls
}
