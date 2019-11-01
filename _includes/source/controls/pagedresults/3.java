SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
SearchOperation search = new SearchOperation(cf);
SearchRequest request = SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(givenName=d*)")
  .returnAttributes("cn", "sn")
  .build();
PagedResultsControl prc = new PagedResultsControl(25); // return 25 entries at a time
request.setControls(prc);
SearchResponse response = new SearchResponse();
byte[] cookie = null;
do {
  prc.setCookie(cookie);
  SearchResponse res = search.execute(request);
  response.addEntries(res.getEntries());
  cookie = null;
  PagedResultsControl ctl = (PagedResultsControl) response.getControl(PagedResultsControl.OID);
  if (ctl != null) {
    if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
      cookie = ctl.getCookie();
    }
  }
} while (cookie != null);
cf.close();
