ExtendedOperation whoami = new ExtendedOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
ExtendedResponse response = whoami.execute(new WhoAmIRequest());
String authzId = WhoAmIResponseParser.parse(response);
