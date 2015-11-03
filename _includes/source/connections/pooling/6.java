BlockingConnectionPool pool = new BlockingConnectionPool(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
pool.setPruneStrategy(new IdlePruneStrategy(900, 1800));
pool.initialize();
