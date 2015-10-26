ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org"); 
ParallelSearchExecutor executor = new ParallelSearchExecutor();
SearchFilter sf1 = new SearchFilter("(givenName=daniel)");
SearchFilter sf2 = new SearchFilter("(sn=fisher)");

// iterates over the the search filters and executes each search on the same connection; null means return all attributes
Collection<Response<SearchResult>> responses = executor.search(
  cf, new SearchFilter[] {sf1, sf2}, (String[]) null);

// cleanup the underlying executor service
executor.shutdown();
