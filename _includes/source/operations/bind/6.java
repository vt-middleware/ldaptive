PooledConnectionFactory cf = PooledConnectionFactory.builder()
  .config(ConnectionConfig.builder()
    .url("ldap://directory.ldaptive.org")
    .useStartTLS(true)
    .connectionInitializers(BindConnectionInitializer.builder()
      .saslConfig(SaslConfig.builder()
        .mechanism(Mechanism.GSSAPI)
        .qualityOfProtection(QualityOfProtection.AUTH_INT)
        .property("org.ldaptive.sasl.gssapi.jaas.principal", "test-principal")
        .property("org.ldaptive.sasl.gssapi.jaas.useKeyTab", "true")
        .property("org.ldaptive.sasl.gssapi.jaas.keyTab", "/etc/krb5.keytab")
        .build())
      .build())
    .build())
  .min(3)
  .max(6)
  .build();
cf.initialize();
// search operation performed as the test-principal user
SearchOperation search = new SearchOperation(cf, "dc=ldaptive,dc=org");
SearchResponse response = search.execute("(uid=dfisher)");
LdapEntry entry = response.getEntry();
cf.close();
