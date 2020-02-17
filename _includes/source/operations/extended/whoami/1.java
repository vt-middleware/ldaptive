ExtendedOperation whoami = new ExtendedOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
ExtendedResponse res = whoami.execute(new WhoAmIRequest());
if (res.isSuccess()) {
  String authzId = WhoAmIResponseParser.parse(res);
}
