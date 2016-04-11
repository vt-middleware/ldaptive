BlockingConnectionPool pool = new BlockingConnectionPool(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
pool.setPruneStrategy(new IdlePruneStrategy(Duration.ofMinutes(15), Duration.ofMinutes(30)));
pool.initialize();
