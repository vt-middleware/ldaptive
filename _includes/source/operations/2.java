DeleteOperation.builder()
  .factory(getConnectionFactory())
  .onResult(result -> {
    if (!result.equals(ResultCode.SUCCESS)) {
      // delete operation was not successful
    }
  })
  .build()
  .send(DeleteRequest.builder()
    .dn("cn=myentry,dc=ldaptive,dc=org")
    .build());
