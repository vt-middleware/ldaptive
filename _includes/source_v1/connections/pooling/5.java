PoolConfig poolConfig = new PoolConfig();
poolConfig.setValidatePeriodically(true);
BlockingConnectionPool pool = new BlockingConnectionPool(poolConfig, new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
pool.setValidator(new SearchValidator());
pool.initialize();
