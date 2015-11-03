Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  AsyncSearchOperation search = new AsyncSearchOperation(conn);
  final BlockingQueue<AsyncRequest> queue = new LinkedBlockingQueue<AsyncRequest>();

  // do something when a response is received
  search.setOperationResponseHandlers(
    new OperationResponseHandler<SearchRequest, SearchResult>() {
      @Override
      public HandlerResult<Response<SearchResult>> handle(
        Connection conn, SearchRequest request, Response<SearchResult> response)
        throws LdapException
      {
        System.err.println("received: " + response);
        return new HandlerResult<Response<SearchResult>>(response);
      }
    });

  // if you plan to cancel or abandon this operation you need a reference to the AsyncRequest
  search.setAsyncRequestHandlers(
    new AsyncRequestHandler() {
      @Override
      public HandlerResult<AsyncRequest> handle(
        Connection conn, Request request, AsyncRequest asyncRequest)
        throws LdapException
      {
        queue.add(asyncRequest);
        return new HandlerResult<AsyncRequest>(asyncRequest);
      }
    });

  // long running searches may experience problems, do something when an exception occurs
  search.setExceptionHandler(
    new ExceptionHandler() {
      @Override
      public HandlerResult<Exception> handle(
        Connection conn, Request request, Exception exception)
      {
        System.err.println("received exception: " + exception);
        return new HandlerResult<Exception>(null);
      }
    });

  SearchRequest request = new SearchRequest("ou=people,dc=ldaptive,dc=org", "(cn=*fisher)");
  // do something when an entry is received
  request.setSearchEntryHandlers(
    new SearchEntryHandler() {
      @Override
      public HandlerResult<SearchEntry> handle(
        Connection conn, SearchRequest request, SearchEntry entry)
        throws LdapException
      {
        System.err.println("received: " + entry);
        // return null value in the handler result to prevent the entry from being stored in the SearchResult
        return new HandlerResult<SearchEntry>(entry);
      }
       @Override
      public void initializeRequest(final SearchRequest request) {}
    });

  FutureResponse<SearchResult> response = search.execute(request);

  // wait for the async handle
  AsyncRequest asyncRequest = queue.take();

  // now you can abandon the operation
  asyncRequest.abandon();

  // or cancel the operation
  CancelOperation cancel = new CancelOperation(conn);
  cancel.execute(new CancelRequest(asyncRequest.getMessageId()));

  // or just block until the response arrives
  SearchResult result = response.getResult();

  // cleanup the underlying executor service
  search.shutdown();

} finally {
  conn.close();
}
