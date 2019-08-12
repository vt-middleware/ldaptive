SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
SearchRequest request1 = new SearchRequest("dc=ldaptive,dc=org","(givenName=daniel)");
SearchRequest request2 = new SearchRequest("dc=ldaptive,dc=org","(sn=fisher)");
SearchOperationWorker search = new SearchOperationWorker(new SearchOperation(cf));

// to perform a single search
Future<SearchResponse> future = search.execute(request1);

// to perform multiple searches
Collection<Future<SearchResponse>> futures = search.execute(request1, request2);

// to perform multiple searches and wait
Collection<SearchResponse> responses = search.executeToCompletion(request1, request2);

// cleanup the underlying executor service
search.shutdown();
cf.close();
