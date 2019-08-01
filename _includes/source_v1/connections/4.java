ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org:389");
connConfig.setUseStartTLS(true);
Connection conn = DefaultConnectionFactory.getConnection(connConfig);
// open a connection and startTLS
conn.open();
