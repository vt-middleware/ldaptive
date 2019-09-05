SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
SearchRequest request1 = new SearchRequest("dc=ldaptive,dc=org","(givenName=daniel)");
SearchRequest request2 = new SearchRequest("dc=ldaptive,dc=org","(sn=fisher)");
SearchOperationWorker search = new SearchOperationWorker(new SearchOperation(cf));

// to perform non-blocking searches
Collection<OperationHandle<SearchRequest, SearchResponse>> futures = search.send(new SearchRequest[] {request1, request2});

// to perform blocking searches
Collection<SearchResponse> responses = search.execute(new SearchRequest[] {request1, request2});

cf.close();
