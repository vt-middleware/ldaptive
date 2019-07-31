DefaultConnectionFactory connFactory = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
connFactory.getProvider().getProviderConfig().setOperationExceptionResultCodes(
  new ResultCode[] {ResultCode.BUSY, ResultCode.UNAVAILABLE, });
Connection conn = connFactory.getConnection();
