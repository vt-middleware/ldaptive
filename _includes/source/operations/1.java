try {
  DeleteOperation.builder()
    .factory(new DefaultConnectionFactory("ldap://directory.ldaptive.org"))
    .throwIf(ResultPredicate.NOT_SUCCESS)
    .build()
    .execute(DeleteRequest.builder()
      .dn("cn=myentry,dc=ldaptive,dc=org")
      .build());
} catch (LdapException e) {
  // delete operation was not successful
}
