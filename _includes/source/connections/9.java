DefaultConnectionFactory connFactory = new DefaultConnectionFactory("ldap://directory-1.ldaptive.org ldap://directory-2.ldaptive.org");
connFactory.getProvider().getProviderConfig().setConnectionStrategy(ConnectionStrategy.ROUND_ROBIN);
Connection conn = connFactory.getConnection();
