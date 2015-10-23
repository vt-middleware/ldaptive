BlockingConnectionPool pool = new BlockingConnectionPool(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
pool.setPruneStrategy(new IdlePoolStrategy(900, 1800));
pool.initialize();
