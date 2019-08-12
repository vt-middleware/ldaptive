ConnectionConfig.builder()
  .url("ldap://directory.ldaptive.org")
  .useStartTLS(true)
  .connectTimeout(Duration.ofSeconds(5))
  .responseTimeout(Duration.ofSeconds(5))
  .sslConfig(SslConfig.builder()
    .hostnameVerifier(new AllowAnyHostnameVerifier())
    .build())
  .build();
