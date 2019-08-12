SingleConnectionFactory cf1 = new SingleConnectionFactory("ldap://directory-1.ldaptive.org");
cf1.initialize();
SingleConnectionFactory cf2 = new SingleConnectionFactory("ldap://directory-2.ldaptive.org");
cf2.initialize();
AggregateSearchOperation search = new AggregateSearchOperation();
SearchFilter sf1 = new SearchFilter("(givenName=daniel)");
SearchFilter sf2 = new SearchFilter("(sn=fisher)");

// iterates over the connection factories and executes each search filter; null means return all attributes
Collection<SearchResponse> responses = search.execute(
  new ConnectionFactory[] {cf1, cf2}, new SearchFilter[] {sf1, sf2}, (String[]) null);

// cleanup the underlying executor service
search.shutdown();
cf1.close();
cf2.close();