// define two connection configs for the servers to aggregate
ConnectionConfig connConfig1 = ConnectionConfig.builder()
  .url("ldap://directory1.ldaptive.org")
  .useStartTLS(true)
  .build();
ConnectionConfig connConfig2 = ConnectionConfig.builder()
  .url("ldap://directory2.ldaptive.org")
  .useStartTLS(true)
  .build();

// use two search dn resolvers
final AggregateDnResolver resolver = AggregateDnResolver.builder()
  .resolver("directory1", SearchDnResolver.builder()
    .factory(new DefaultConnectionFactory(connConfig1))
    .dn("ou=people,dc=ldaptive,dc=org")
    .filter("(uid={user})")
    .build())
  .resolver("directory2", SearchDnResolver.builder()
    .factory(new DefaultConnectionFactory(connConfig2))
    .dn("ou=accounts,dc=ldaptive,dc=org")
    .filter("(mail={user})")
    .build())
  .build();

// use two bind authentication handlers
final AggregateAuthenticationHandler handler = AggregateAuthenticationHandler.builder()
  .handler("directory1", new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(connConfig1)))
  .handler("directory2", new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(connConfig2)))
  .build();

// create an authenticator that aggregates over both directories
Authenticator auth = new Authenticator(resolver, handler);
