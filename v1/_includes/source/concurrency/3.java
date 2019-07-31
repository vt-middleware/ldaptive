ConnectionFactory cf1 = new DefaultConnectionFactory("ldap://directory-1.ldaptive.org");
ConnectionFactory cf2 = new DefaultConnectionFactory("ldap://directory-2.ldaptive.org");
AggregateSearchExecutor executor = new AggregateSearchExecutor();
SearchFilter sf1 = new SearchFilter("(givenName=daniel)");
SearchFilter sf2 = new SearchFilter("(sn=fisher)");

// iterates over the connection factories and executes each search filter; null means return all attributes
Collection<Response<SearchResult>> responses = executor.search(
  new ConnectionFactory[] {cf1, cf2}, new SearchFilter[] {sf1, sf2}, (String[]) null);

// cleanup the underlying executor service
executor.shutdown();
