PooledConnectionFactory cf = PooledConnectionFactory.builder()
  .config(ConnectionConfig.builder()
    .url("ldap://directory.ldaptive.org")
    .useStartTLS(true)
    .sslConfig(SslConfig.builder()
      .credentialConfig(X509CredentialConfig.builder()
        .trustCertificates("file:/tmp/certs.pem")
        .authenticationCertificate("file:/tmp/mycert.pem")
        .authenticationKey("file:/tmp/mykey.pkcs8")
        .build())
      .build())
    .connectionInitializers(BindConnectionInitializer.builder()
      .saslConfig(SaslConfig.builder()
        .mechanism(Mechanism.EXTERNAL)
        .build())
      .build())
    .build())
  .min(3)
  .max(6)
  .build();
cf.initialize();
// search operation performed as the external user
SearchOperation search = new SearchOperation(cf, "dc=ldaptive,dc=org");
SearchResponse response = search.execute("(uid=dfisher)");
LdapEntry entry = response.getEntry();
cf.close();
