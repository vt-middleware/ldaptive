PoolConfig poolConfig = new PoolConfig();
poolConfig.setValidateOnCheckIn(true);
BlockingConnectionPool pool = new BlockingConnectionPool(poolConfig, new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
// perform a compare on the ou: people attribute for the ou=people,dc=vt,dc=edu dn
CompareValidator validator = new CompareValidator(new CompareRequest("ou=people,dc=vt,dc=edu", new LdapAttribute("ou", "people")));
pool.setValidator(validator);
pool.initialize();
