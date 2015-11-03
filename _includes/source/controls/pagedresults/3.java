Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn);
  SearchRequest request = new SearchRequest("dc=ldaptive,dc=org","(givenName=d*)", "cn", "sn");
  PagedResultsControl prc = new PagedResultsControl(25); // return 25 entries at a time
  request.setControls(prc);
  SearchResult result = new SearchResult();
  byte[] cookie = null;
  do {
    prc.setCookie(cookie);
    Response<SearchResult> response = search.execute(request);
    result.addEntries(response.getResult().getEntries());
    cookie = null;
    PagedResultsControl ctl = (PagedResultsControl) response.getControl(PagedResultsControl.OID);
    if (ctl != null) {
      if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
        cookie = ctl.getCookie();
      }
    }
  } while (cookie != null);
} finally { 
  conn.close();
}
