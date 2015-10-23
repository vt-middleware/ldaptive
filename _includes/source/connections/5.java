ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);

KeyStoreCredentialConfig credConfig = new KeyStoreCredentialConfig();
credConfig.setTrustStore("classpath:/my.truststore");

connConfig.setSslConfig(new SslConfig(credConfig));
Connection conn = DefaultConnectionFactory.getConnection(connConfig);
