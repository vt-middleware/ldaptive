ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);

X509CredentialConfig credConfig = new X509CredentialConfig();
credConfig.setTrustCertificates("file:/tmp/certs.pem");
credConfig.setAuthenticationCertificate("file:/tmp/mycert.pem");
credConfig.setAuthenticationKey("file:/tmp/mykey.pkcs8");

connConfig.setSslConfig(new SslConfig(credConfig));
Connection conn = DefaultConnectionFactory.getConnection(connConfig);
