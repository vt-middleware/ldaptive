SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
BindOperation bind = new BindOperation(cf);
BindResponse res = bind.execute(new SASLBindRequest("EXTERNAL"));
if (res.isSuccess()) {
  // bind succeeded
} else {
  // bind failed
}
cf.close();
