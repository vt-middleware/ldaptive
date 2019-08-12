StringWriter writer = new StringWriter();
JsonWriter jsonWriter = new JsonWriter(writer);
SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchResponse response = search.execute(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(uid=dfisher)")
  .attributes("mail")
  .build());
jsonWriter.write(response);
System.out.println(writer.toString());
