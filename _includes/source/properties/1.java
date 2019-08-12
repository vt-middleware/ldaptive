java.util.Properties props = new java.util.Properties();
props.load(new FileInputStream("/path/to/ldap.properties"));

DefaultConnectionFactory connectionFactory = new DefaultConnectionFactory();
DefaultConnectionFactoryPropertySource dcfSource = new DefaultConnectionFactoryPropertySource(connectionFactory, props);
dcfSource.initialize();

SearchRequest request = new SearchRequest();
SearchRequestPropertySource srSource = new SearchRequestPropertySource(request, props);
srSource.initialize();

SearchOperation search = new SearchOperation(connectionFactory);
SearchResponse response = SearchOperation.execute(connectionFactory, request);
