SearchOperation search = new SearchOperation(new DefaultConnectionFactory("ldap://directory.ldaptive.org"));
SearchResponse res = search.execute(SearchRequest.builder()
  .dn("dc=ldaptive,dc=org")
  .filter("(&(givenName=daniel)(sn=fisher))")
  .returnAttributes("userCertificate;binary")
  .build());
LdapEntry entry = res.getEntry();
Certificate cert = entry.getAttribute("userCertificate;binary").getValue(new CertificateValueTranscoder().decoder());
