StringWriter writer = new StringWriter();
Dsmlv1Writer dsmlWriter = new Dsmlv1Writer(writer);
Connection conn = DefaultConnectionFactory.getConnection("ldap://directory.ldaptive.org"); 
try { 
  conn.open(); 
  SearchOperation search = new SearchOperation(conn);
  SearchResult result = search.execute(
    new SearchRequest("dc=ldaptive,dc=org", new SearchFilter("(uid=dfisher)"), new String[] {"mail"})).getResult();
  dsmlWriter.write(result);
  System.out.println(writer.toString());
} finally { 
  conn.close();
}
