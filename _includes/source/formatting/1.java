StringWriter writer = new StringWriter();
LdifWriter ldifWriter = new LdifWriter(writer);
SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchResponse response = search.execute(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(uid=dfisher)")
  .returnAttributes("mail")
  .build());
ldifWriter.write(response);
System.out.println(writer.toString());
