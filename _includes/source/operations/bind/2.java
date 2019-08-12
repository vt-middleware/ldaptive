SingleConnectionFactory cf = new SingleConnectionFactory("ldap://directory.ldaptive.org");
cf.initialize();
BindOperation bind = new BindOperation(cf);
BindResponse res = bind.execute(new AnonymousBindRequest());
if (res.isSuccess()) {
  // bind succeeded
} else {
  // bind failed
}
// use the connection factory to perform anonymous operations
cf.close();
