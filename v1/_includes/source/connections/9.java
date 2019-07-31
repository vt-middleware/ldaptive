ConnectionConfig connConfig = new ConnectionConfig("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org");
connConfig.setConnectionStrategy(new RoundRobinConnectionStrategy());
DefaultConnectionFactory connFactory = new DefaultConnectionFactory(connConfig);
Connection conn = connFactory.getConnection();
