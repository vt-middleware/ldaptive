Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
SearchOperation search = new SearchOperation(conn);
SearchOperation.ReopenOperationExceptionHandler handler = search.new ReopenOperationExceptionHandler();
handler.setRetry(5); // retry operations 5 times
handler.setRetryWait(3000); // wait 3 seconds between retries
handler.setRetryBackoff(2); // set a backoff factor of 2
search.setOperationExceptionHandler(handler);
