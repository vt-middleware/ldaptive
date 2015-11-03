StringWriter writer = new StringWriter();
LdifWriter ldifWriter = new LdifWriter(writer);
Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org");
try {
  conn.open();
  SearchOperation search = new SearchOperation(conn);
  SearchResult result = search.execute(
    new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("(uid=dfisher)"), new String[] {"mail"})).getResult();
  ldifWriter.write(result);
  System.out.println(writer.toString());
} finally {
  conn.close();
}
