SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
ParallelSearchOperation search = new ParallelSearchOperation();
SearchFilter sf1 = new SearchFilter("(givenName=daniel)");
SearchFilter sf2 = new SearchFilter("(sn=fisher)");

// iterates over the the search filters and executes each search on the same connection; null means return all attributes
Collection<SearchResponse> responses = search.execute(cf, new SearchFilter[] {sf1, sf2}, (String[]) null);

// cleanup the underlying executor service
search.shutdown();
cf.close();
