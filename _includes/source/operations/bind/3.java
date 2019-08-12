SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
BindOperation bind = new BindOperation(cf);
BindResponse res = bind.execute(new SaslBindRequest("EXTERNAL"));
if (res.isSuccess()) {
  // bind succeeded
} else {
  // bind failed
}
cf.close();
