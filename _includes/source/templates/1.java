SearchTemplates oneTermTemplate = new SearchTemplates(
  "(|(telephoneNumber={term1})(localPhone={term1}))",
  "(|(telephoneNumber=*{term1})(localPhone=*{term1}))",
  "(|(givenName={term1})(sn={term1}))",
  "(|(givenName={term1}*)(sn={term1}*))",
  "(|(givenName=*{term1}*)(sn=*{term1}*))");

SearchTemplates twoTermTemplate = new SearchTemplates(
  "(&(givenName={term1})(sn={term2}))",
  "(cn={term1} {term2})",
  "(&(givenName={term1}*)(sn={term2}*))",
  "(cn={term1}* {term2}*)",
  "(&(givenName=*{term1}*)(sn=*{term2}*))",
  "(cn=*{term1}* *{term2}*)");

SearchTemplates threeTermTemplate = new SearchTemplates(
  "(|(&(givenName={term1})(sn={term3}))(&(givenName={term2})(sn={term3})))",
  "(|(cn={term1} {term2} {term3})(cn={term2} {term1} {term3}))",
  "(|(&(givenName={term1}*)(sn={term3}*))(&(givenName={term2}*)(sn={term3}*)))",
  "(|(cn={term1}* {term2}* {term3}*)(cn={term2}* {term1}* {term3}*))",
  "(|(&(givenName=*{term1}*)(sn=*{term3}*))(&(givenName=*{term2}*)(sn=*{term3}*)))",
  "(|(cn=*{term1}* *{term2}* *{term3}*)(cn=*{term2}* *{term1}* *{term3}*))",
  "(|(&(givenName={term1})(middleName={initial2}*)(sn={term3}))(&(givenName={term2})(middleName={initial1}*)(sn={term3})))",
  "(|(&(givenName={initial1}*)(middlename={initial2}*)(sn={term3}))(&(givenName={initial2}*)(middleName={initial1}*)(sn={term3})))",
  "(sn={term3})");

// create a pooled connection factory for searching
PooledConnectionFactory cf = new PooledConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();

SearchTemplatesExecutor executor = new SearchTemplatesExecutor(
  new AggregateSearchExecutor(),
  new PooledConnectionFactory[] {cf},
  oneTermTemplate,
  twoTermTemplate,
  threeTermTemplate);

// get results for a one term query
Query oneTermQuery = new Query("fisher");
SearchResponse oneTermResult = executor.search(oneTermQuery);

// get results for a two term query
Query twoTermQuery = new Query("daniel fisher");
SearchResponse twoTermResult = executor.search(twoTermQuery);

// get results for a three term query
Query threeTermQuery = new Query("daniel william fisher");
SearchResponse threeTermResult = executor.search(threeTermQuery);
