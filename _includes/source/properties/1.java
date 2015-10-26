Properties props = new Properties();
props.load(new FileInputStream("/path/to/ldap.properties"));

SearchExecutor searchExecutor = new SearchExecutor();
SearchRequestPropertySource srSource = new SearchRequestPropertySource(searchExecutor, props);
srSource.initialize();

DefaultConnectionFactory connectionFactory = new DefaultConnectionFactory();
DefaultConnectionFactoryPropertySource dcfSource = new DefaultConnectionFactoryPropertySource(connectionFactory, props);
dcfSource.initialize();

SearchResult result = searchExecutor.search(connectionFactory, "(mail=dfisher@ldaptive.org)").getResult();
