// define two connection configs for the servers to aggregate
ConnectionConfig connConfig1 = new ConnectionConfig("ldap://directory1.ldaptive.org");
connConfig1.setUseStartTLS(true);
ConnectionConfig connConfig2 = new ConnectionConfig("ldap://directory2.ldaptive.org");
connConfig2.setUseStartTLS(true);

// use two search dn resolvers
SearchDnResolver dnResolver1 = new SearchDnResolver(new DefaultConnectionFactory(connConfig1));
dnResolver1.setBaseDn("ou=people,dc=ldaptive,dc=org");
dnResolver1.setUserFilter("(uid={user})");

SearchDnResolver dnResolver2 = new SearchDnResolver(new DefaultConnectionFactory(connConfig2));
dnResolver2.setBaseDn("ou=accounts,dc=ldaptive,dc=org");
dnResolver2.setUserFilter("(mail={user})");

final Map<String, DnResolver> resolvers = new HashMap<>();
resolvers.put("directory1", dnResolver1);
resolvers.put("directory2", dnResolver2);

final AggregateDnResolver resolver = new AggregateDnResolver(resolvers);

// use two bind authentication handlers
BindAuthenticationHandler authHandler1 = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig1));
BindAuthenticationHandler authHandler2 = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig2));
final Map<String, AuthenticationHandler> handlers = new HashMap<>();
handlers.put("directory1", authHandler1);
handlers.put("directory2", authHandler2);

final AggregateDnResolver.AuthenticationHandler handler = new AggregateDnResolver.AuthenticationHandler(handlers);

// create an authenticator that aggregates over both directories
Authenticator auth = new Authenticator(resolver, handler);
