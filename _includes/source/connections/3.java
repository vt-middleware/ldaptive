ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org:636");
connConfig.setUseSSL(true);
Connection conn = DefaultConnectionFactory.getConnection(connConfig);
// open an SSL connection
conn.open();
